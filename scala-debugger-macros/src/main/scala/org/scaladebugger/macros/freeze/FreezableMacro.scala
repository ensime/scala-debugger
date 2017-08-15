package org.scaladebugger.macros.freeze

import scala.language.experimental.macros
import scala.annotation.tailrec
import scala.reflect.macros.whitebox
import macrocompat.bundle

@bundle class FreezableMacro(val c: whitebox.Context) {
  import c.universe._

  // NOTE: Stuck with c.Expr[Any] instead of c.Tree for now;
  //       blows up in Scala 2.10 even with @bundle otherwise
  def impl(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val (annottee, expandees) = annottees.map(_.tree) match {
      case (classDef: ClassDef) :: (moduleDef: ModuleDef) :: Nil if isTrait(classDef) =>
        //println("INPUT CLASS AND MODULE :: " + classDef + " :: " + moduleDef)
        val results = processTraitAndObj(classDef, moduleDef)
        //println("RESULTS :: " + results)
        (EmptyTree, results)
      case (classDef: ClassDef) :: Nil if isTrait(classDef) =>
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

  private def containsAnnotation(d: DefDef, aName: String): Boolean =
    findAnnotation(d, aName).nonEmpty

  private def findAnnotation(d: DefDef, aName: String): Option[Tree] =
    d.mods.annotations.find(a => isAnnotation(a, aName))

  private def isAnnotation(t: Tree, aName: String): Boolean = t match {
    case Apply(f, args) => f match {
      case Select(qual, name) => qual match {
        case New(tpt) => tpt match {
          case Ident(iName) => iName.decodedName.toString == aName
          case _ => false
        }
        case _ => false
      }
    }
    case _ => false
  }

  private def isTrait(classDef: ClassDef): Boolean = {
    try {
      val q"""
          $mods trait $tpname[..$tparams] extends {
            ..$earlydefns
          } with ..$parents { $self =>
            ..$body
          }
        """ = classDef
      true
    } catch {
      case _: Throwable => false
    }
  }

  private def extractInheritedMethods(parents: List[Type]): List[MethodSymbol] = {
    val anyTypeSymbol = typeOf[Any].typeSymbol
    val objectTypeSymbol = typeOf[Object].typeSymbol

    def stripDuplicateMethods(methods: List[MethodSymbol]): List[MethodSymbol] = {
      val overrides = methods.flatMap(_.overrides)
      methods.filterNot(overrides.contains)
    }

    @tailrec def extract(types: List[Type], methods: List[MethodSymbol]): List[MethodSymbol] = {
      val parentMethods = types.flatMap(pt => {
        pt.members
          .filter(_.isMethod)
          .filter(_.owner == pt.typeSymbol)
          .map(_.asMethod)
      })
      val parentTypes = types
        .flatMap(t => {
          t.baseClasses.filterNot(c =>
            c == t.typeSymbol ||
            c == anyTypeSymbol ||
            c == objectTypeSymbol
          )
        })
        .filter(_.isClass)
        .map(_.asClass.toType)

      val allMethods = stripDuplicateMethods(methods ++ parentMethods)

      if (parentTypes.nonEmpty) extract(parentTypes, allMethods)
      else allMethods
    }

    extract(parents, Nil)
  }

  private def extractParentTypes(parents: List[Tree]): List[Option[Type]] = {
    parents match {
      case p: List[Tree] => p.map(extractParentType)
    }
  }

  private def extractParentType(parent: Tree): Option[Type] = {
    def isBaseType(n1: Name, n2: Name = null): Boolean = {
      val name = if (n2 != null) {
        n1.toString + "." + n2.toString
      } else {
        n1.toString
      }

      val baseTypeNames = Seq(
        "Any", "scala.Any", "AnyRef", "scala.AnyRef",
        "AnyVal", "scala.AnyVal", "java.lang.Object"
      )

      baseTypeNames.contains(name)
    }

    parent match {
      case Ident(name) if !isBaseType(name) =>
        val typeName = name.toTypeName
        val typedTree = scala.util.Try(c.typecheck(q"null.asInstanceOf[$typeName]"))
        typedTree.toOption.flatMap(tt => Option(tt.tpe))
      case Select(Ident(name), typeName) if !isBaseType(name, typeName) =>
        val t1 = name.toTermName
        val t2 = typeName.toTypeName
        val typedTree = scala.util.Try(c.typecheck(q"null.asInstanceOf[$t1.$t2]"))
        typedTree.toOption.flatMap(tt => Option(tt.tpe))
      case a =>
        None
    }
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
    val moduleDef: ModuleDef = q"object $oName {}" match {
      case m: ModuleDef => m
    }

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

    val inheritedMethods = extractInheritedMethods(
      extractParentTypes(parents.asInstanceOf[List[Tree]]).flatten
    )

    // Represents name of variable passed to freeze method
    val freezeObjName = TermName("valueToFreeze")

    val freezeTrees = {
      val cParams = collection.mutable.ArrayBuffer[Tree]()
      val cArgs = collection.mutable.ArrayBuffer[Tree]()
      val bodyTrees: List[Tree] = body match {
        case l: List[Tree] => l
      }
      val internalMethods = bodyTrees.collect { case d: DefDef => d }

      // TODO: Also process inherited methods that are not covered
      //       by the tree methods
      // TODO: CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP CHIP
      val internals = internalMethods.map {
        case d: DefDef if containsAnnotation(d, "CanFreeze") =>
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
          findAnnotation(d, "CanFreeze").get match {
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
          val newMods = c.universe.Modifiers(
            flags = Flag.OVERRIDE | oldMods.flags,
            privateWithin = oldMods.privateWithin,
            annotations = oldMods.annotations.filterNot(a => {
              isAnnotation(a, "CanFreeze") || isAnnotation(a, "CannotFreeze")
            })
          )

          q"""
            $newMods def $tname[..$tparams](...$paramss): $tpt = {
              this.$name.get
            }
          """
        case d: DefDef if containsAnnotation(d, "CannotFreeze") =>
          val q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" = d

          // Add override if not already there
          val oldMods: Modifiers = mods match {
            case m: Modifiers => m
          }
          val newMods = c.universe.Modifiers(
            flags = Flag.OVERRIDE | oldMods.flags,
            privateWithin = oldMods.privateWithin,
            annotations = oldMods.annotations.filterNot(a => {
              isAnnotation(a, "CanFreeze") || isAnnotation(a, "CannotFreeze")
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
        final class Frozen(..$cParams) extends $tpname with java.io.Serializable { $self =>
          ..$internals
        }
      """
      val method = q"""
        def freeze($freezeObjName: $tpname): $tpname = new Frozen(..$cArgs)
      """

      List(klass, method)
    }

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

      tree match { case c: ClassDef => c }
    }

    val newModuleDef: ModuleDef = {
      val q"""
        $mods object $tname extends {
          ..$earlydefns
        } with ..$parents { $self =>
          ..$body
        }
      """ = moduleDef

      val oldBody: List[Tree] = body match {
        case l: List[Tree] => l
      }
      val newBody = oldBody ++ freezeTrees

      val newObjDef = q"""
          $mods object $tname extends {
            ..$earlydefns
          } with ..$parents { $self =>
            ..$newBody
          }
        """

      newObjDef match { case m: ModuleDef => m }
    }

    List(newClassDef, newModuleDef)
  }
}

