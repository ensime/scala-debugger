package org.scaladebugger.macros.freeze

import scala.language.experimental.macros
import scala.annotation.tailrec
import scala.reflect.macros.whitebox
import macrocompat.bundle

@bundle class FreezableMacro(val c: whitebox.Context) {
  // NOTE: Stuck with c.Expr[Any] instead of c.Tree for now;
  //       blows up in Scala 2.10 even with @bundle otherwise
  def impl(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    def findClassPackage(classDef: ClassDef): TermName = {
      val dummyClassName = TypeName(s"Dummy${TypeName(c.freshName())}")
      val dummyClass = c.typecheck(q"class $dummyClassName")
      TermName(packageFromSymbol(dummyClass.symbol.owner))
    }

    def packageFromSymbol(symbol: Symbol): String = {
      @tailrec def buildPackage(symbol: Symbol, tokens: Seq[String]): String = {
        if (symbol == NoSymbol) tokens.mkString(".")
        else buildPackage(symbol.owner, symbol.name.decodedName.toString +: tokens)
      }

      buildPackage(symbol, Nil)
    }

    def containsAnnotation(d: DefDef, aName: String): Boolean =
      d.mods.annotations.exists(a => isAnnotation(a, aName))

    def isAnnotation(t: Tree, aName: String): Boolean = t match {
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

    def isTrait(classDef: ClassDef): Boolean = {
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

    def processTrait(classDef: ClassDef): List[Tree] = {
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

    def processTraitAndObj(
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

      // Represents name of variable passed to freeze method
      val freezeObjName = TermName("valueToFreeze")

      val (frozenClass, freezeMethod) = {
        val cParams = collection.mutable.ArrayBuffer[Tree]()
        val cArgs = collection.mutable.ArrayBuffer[Tree]()
        val bodyTrees: List[Tree] = body match {
          case l: List[Tree] => l
        }
        val internals = bodyTrees.collect {
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
            cArgs.append(q"""
              scala.util.Try.apply($freezeObjName.$tname)
            """)

            // Add override if not already there
            val oldMods: Modifiers = mods match {
              case m: Modifiers => m
            }
            val newMods = c.universe.Modifiers(
              flags = Flag.OVERRIDE | oldMods.flags,
              privateWithin = oldMods.privateWithin,
              annotations = oldMods.annotations.filterNot(a => {
                isAnnotation(a, "CanFreeze") ||
                  isAnnotation(a, "CannotFreeze")
              })
            )

            q"$newMods def $tname[..$tparams](...$paramss): $tpt = this.$name.get"
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
                isAnnotation(a, "CanFreeze") ||
                  isAnnotation(a, "CannotFreeze")
              })
            )

            q"""
              $newMods def $tname[..$tparams](...$paramss): $tpt =
                throw new IllegalStateException("Method not frozen!")
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
          class Frozen(..$cParams) extends $tpname with java.io.Serializable { $self =>
              ..$internals
          }
        """
        val method = q"""
          def freeze($freezeObjName: $tpname): Frozen = new Frozen(..$cArgs)
        """

        (klass, method)
      }

      // TODO: Remove this hack that is injecting ??? into all
      //       non-implemented methods
      val newClassDef: ClassDef = {
        val bodyTrees: List[Tree] = body match {
          case l: List[Tree] => l
        }
        val newBody = bodyTrees.map {
          /*case vDef: ValDef if vDef.rhs.isEmpty => ValDef(
            mods = vDef.mods,
            name = vDef.name,
            tpt = vDef.tpt,
            rhs = q"throw new NotImplementedError"
          )
          case dDef: DefDef if dDef.rhs.isEmpty => DefDef(
            mods = dDef.mods,
            name = dDef.name,
            tparams = dDef.tparams,
            vparamss = dDef.vparamss,
            tpt = dDef.tpt,
            rhs = q"throw new NotImplementedError"
          )*/
          case t => t
        }

        val tree = q"""
          $mods trait $tpname[..$tparams] extends {
            ..$earlydefns
          } with ..$parents { $self =>
            ..$newBody
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

        val implicitMethodName = TypeName(s"${tpname}FrozenWrapper")
        val implicitFreezeMethod = q"""
          implicit class $implicitMethodName(private val $freezeObjName: $tpname) {
            def freeze(): Frozen = $tname.freeze($freezeObjName)
          }
        """

        val oldBody: List[Tree] = body match {
          case l: List[Tree] => l
        }
        val newBody = oldBody ++ List(
          frozenClass,
          freezeMethod,
          q"object Implicits { $implicitFreezeMethod }"
        )

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

    val (annottee, expandees) = annottees.map(_.tree) match {
      case (classDef: ClassDef) :: (moduleDef: ModuleDef) :: Nil if isTrait(classDef) =>
        println("INPUT CLASS AND MODULE :: " + classDef + " :: " + moduleDef)
        val results = processTraitAndObj(classDef, moduleDef)
        println("RESULTS :: " + results)
        (EmptyTree, results)
      case (classDef: ClassDef) :: Nil if isTrait(classDef) =>
        println("INPUT CLASS ONLY :: " + classDef)
        val results = processTrait(classDef)
        println("RESULTS :: " + results)
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
}

