package org.scaladebugger.macros.freeze

import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import macrocompat.bundle
import org.scaladebugger.macros.MacroUtils

@bundle class FreezableMacro(val c: whitebox.Context) {
  import c.universe._

  val M = new MacroUtils[c.type](c)

  // NOTE: Stuck with c.Expr[Any] instead of c.Tree for now;
  //       blows up in Scala 2.10 even with @bundle otherwise
  def impl(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val (annottee, expandees) = annottees.map(_.tree) match {
      case (classDef: ClassDef) :: (moduleDef: ModuleDef) :: Nil if M.isTrait(classDef) =>
        //println("INPUT CLASS AND MODULE :: " + classDef + " :: " + moduleDef)
        val results = processTraitAndObj(classDef, moduleDef)
        //println("RESULTS :: " + results)
        (EmptyTree, results)
      case (classDef: ClassDef) :: Nil if M.isTrait(classDef) =>
        //println("INPUT CLASS ONLY :: " + classDef)
        val results = processTrait(classDef)
        //println("RESULTS :: " + results)
        (EmptyTree, results)
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Freezable can only be placed on traits!"
        )
    }

    val outputs = expandees
    c.Expr[Any](
      Block(outputs, Literal(Constant(())))
    )
  }

  private def processTrait(classDef: ClassDef): List[Tree] = {
    val q"""
        $mods trait $tpname[..$tparams] extends {
          ..$earlydefns
        } with ..$parents { $self =>
          ..$body
        }
      """ = classDef

    // Represents name of companion object
    val traitTypeName: TypeName = tpname match {
      case t: TypeName => t
    }
    val oName = TermName(traitTypeName.toString)

    // Generate a new class containing the helper methods and
    // object containing "Frozen" class representation
    val moduleDef = M.newEmptyObject(traitTypeName.toString)

    processTraitAndObj(classDef, moduleDef)
  }

  private def processTraitAndObj(
    classDef: ClassDef,
    moduleDef: ModuleDef
  ): List[Tree] = {
    val q"""
      $mods trait $tpname[..$tparams] extends {
        ..$earlydefns
      } with ..$parents { $self =>
        ..$body
      }
    """ = classDef

    val inheritedMethods = M.extractInheritedMethods(
      M.extractParentTypes(parents.asInstanceOf[List[Tree]]).flatten
    )

    // Represents name of variable passed to freeze method

    val freezeTrees = generateTrees(
      body match { case l: List[Tree] => l },
      tpname match { case t: TypeName => t }
    )

    // TODO: Inject freeze method directly into trait rather than
    //       relying on object implicit
    val newClassDef: ClassDef = {
      val bodyTrees: List[Tree] = body match {
        case l: List[Tree] => l
      }

      // TODO: Find better way to know when to mark as overriden
      val o = TermName(tpname.toString())
      val freezeMethod =
        if (inheritedMethods.exists(_.name.toString == "freeze"))
          q"override def freeze(): $tpname = $o.freeze(this)"
        else
          q"def freeze(): $tpname = $o.freeze(this)"

      val tree = q"""
        $mods trait $tpname[..$tparams] extends {
          ..$earlydefns
        } with ..$parents { $self =>
          ..$bodyTrees
          $freezeMethod
        }
      """

      tree match {
        case c: ClassDef => c
      }
    }

    val newModuleDef: ModuleDef = {
      val q"""
        $mods object $tname extends {
          ..$earlydefns
        } with ..$parents { $self =>
          ..$body
        }
      """ = moduleDef

      val newObjDef = q"""
        $mods object $tname extends {
          ..$earlydefns
        } with ..$parents { $self =>
          ..$body
          ..$freezeTrees
        }
      """

      newObjDef match { case m: ModuleDef => m }
    }

    List(newClassDef, newModuleDef)
  }

  private def generateTrees(body: List[Tree], traitTypeName: TypeName): List[Tree] = {
    val freezeObjName = TermName("valueToFreeze")
    val cParams = collection.mutable.ArrayBuffer[Tree]()
    val cArgs = collection.mutable.ArrayBuffer[Tree]()
    val bodyTrees: List[Tree] = body match {
      case l: List[Tree] => l
    }
    val internalMethods = bodyTrees.collect { case d: DefDef => d }

    // TODO: Also process inherited methods that are not covered
    //       by the tree methods
    // TODO: CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP
    //
    // See https://github.com/paulbutcher/ScalaMock/blob/master/shared/src/main/scala/org/scalamock/clazz/MockMaker.scala
    // for example of loading inherited methods in method symbols not found in object
    val internals = internalMethods.map {
      case d: DefDef if M.containsAnnotation(d, "CanFreeze") =>
        val q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" = d

        val paramTrees: List[List[ValDef]] = paramss match {
          case l: List[List[ValDef]] => l
        }
        if (paramTrees.nonEmpty) c.abort(
          c.enclosingPosition,
          s"Unable to freeze $tname! Must have zero arguments!"
        )

        val name = TermName(s"$$$tname")
        cParams.append(q"""
            private val $name: scala.util.Try[$tpt]
          """)

        // TODO: Remove runtime checks by getting better understanding of
        //       return type, if it contains a freeze method, etc.
        M.findAnnotation(d, "CanFreeze").get match {
          case q"new CanFreeze()" =>
            cArgs.append(q"""
                scala.util.Try.apply($freezeObjName.$tname)
                  .asInstanceOf[scala.util.Try[$tpt]]
              """)
          case q"new CanFreeze($returnType)" =>
            // TODO: Find better way to match against the input
            import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
            val r = ReturnType.withName(returnType.toString().split('.').last)

            r match {
              case ReturnType.Normal =>
                cArgs.append(q"""
                    scala.util.Try.apply($freezeObjName.$tname)
                      .asInstanceOf[scala.util.Try[$tpt]]
                  """)
              case ReturnType.FreezeObject =>
                cArgs.append(q"""
                    scala.util.Try.apply($freezeObjName.$tname).map(_.freeze())
                      .asInstanceOf[scala.util.Try[$tpt]]
                  """)
              case ReturnType.FreezeCollection =>
                cArgs.append(q"""
                    scala.util.Try.apply($freezeObjName.$tname).map(_.map(_.freeze()))
                      .asInstanceOf[scala.util.Try[$tpt]]
                  """)
              case ReturnType.FreezeOption =>
                cArgs.append(q"""
                    scala.util.Try.apply($freezeObjName.$tname).map(_.map(_.freeze()))
                      .asInstanceOf[scala.util.Try[$tpt]]
                  """)
              case ReturnType.FreezeEither =>
                cArgs.append(q"""
                    scala.util.Try.apply($freezeObjName.$tname).map(r => r match {
                      case scala.util.Left(o) => scala.util.Left(o.freeze())
                      case scala.util.Right(o) => scala.util.Right(o.freeze())
                    }).asInstanceOf[scala.util.Try[$tpt]]
                  """)
            }
        }

        // Add override if not already there
        val oldMods: Modifiers = mods match {
          case m: Modifiers => m
        }

        // TODO: Is there a way to filter out just the DEFERRED flag?
        val newFlags = Flag.OVERRIDE |
          (if (oldMods.hasFlag(Flag.FINAL)) Flag.FINAL else NoFlags)

        val newMods = c.universe.Modifiers(
          flags = newFlags,
          privateWithin = oldMods.privateWithin,
          annotations = oldMods.annotations.filterNot(a => {
            M.isAnnotation(a, "CanFreeze") ||
              M.isAnnotation(a, "CannotFreeze")
          })
        )

        q"""
            $newMods def $tname[..$tparams](...$paramss): $tpt = {
              this.$name.get
            }
          """
      case d: DefDef if M.containsAnnotation(d, "CannotFreeze") =>
        val q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" = d

        // Add override if not already there
        val oldMods: Modifiers = mods match {
          case m: Modifiers => m
        }

        // TODO: Is there a way to filter out just the DEFERRED flag?
        val newFlags = Flag.OVERRIDE |
          (if (oldMods.hasFlag(Flag.FINAL)) Flag.FINAL else NoFlags)

        val newMods = c.universe.Modifiers(
          flags = newFlags,
          privateWithin = oldMods.privateWithin,
          annotations = oldMods.annotations.filterNot(a => {
            M.isAnnotation(a, "CanFreeze") ||
              M.isAnnotation(a, "CannotFreeze")
          })
        )

        q"""
            $newMods def $tname[..$tparams](...$paramss): $tpt = {
              throw new IllegalStateException("Method not frozen!")
            }
          """
      case d: DefDef =>
        val q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" = d
        val exprTree: Tree = expr match {
          case t: Tree => t
        }
        if (exprTree.isEmpty) c.abort(
          c.enclosingPosition,
          s"$tname has no implementation and is not marked as freezable or unfreezable!"
        )
        null
    }.filterNot(_ == null)

    val klass = q"""
        final class Frozen(..$cParams) extends $traitTypeName with java.io.Serializable {
          ..$internals
        }
      """
    val method = q"""
        def freeze($freezeObjName: $traitTypeName): $traitTypeName = new Frozen(..$cArgs)
      """

    List(klass, method)
  }
}

