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
    val id = java.util.UUID.randomUUID().toString
    val (annottee, expandees) = annottees.map(_.tree) match {
      case (classDef: ClassDef) :: (moduleDef: ModuleDef) :: Nil if M.isTrait(classDef) =>
        println("PROCESSING -- " + classDef.name.decodedName.toString + " -- " + id)
        //println("INPUT CLASS AND MODULE :: " + classDef + " :: " + moduleDef)
        val results = processTraitAndObj(classDef, moduleDef)
        //println("RESULTS :: " + results)
        println("DONE -- " + id)
        (EmptyTree, results)
      case (classDef: ClassDef) :: Nil if M.isTrait(classDef) =>
        println("PROCESSING -- " + classDef.name.decodedName.toString + " -- " + id)
        //println("INPUT CLASS ONLY :: " + classDef)
        val results = processTrait(classDef)
        //println("RESULTS :: " + results)
        if (classDef.name.decodedName.toString == "ValueInfo")
          println("RESULTS :: " + results)
        println("DONE -- " + id)
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

    val parentTypes = M.extractTypes(parents.asInstanceOf[List[Tree]]).flatten
    val inheritedMethods = M.extractInheritedMethods(parentTypes)

    val freezeTrees = generateTrees(
      body match { case l: List[Tree] => l },
      tpname match { case t: TypeName => t },
      parentTypes,
      inheritedMethods
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

  private def markModifiersOverride(modifiers: Modifiers): Modifiers = {
    // TODO: Is there a way to filter out just the DEFERRED flag?
    val newFlags = Flag.OVERRIDE |
      (if (modifiers.hasFlag(Flag.FINAL)) Flag.FINAL else NoFlags)

    c.universe.Modifiers(
      flags = newFlags,
      privateWithin = modifiers.privateWithin,
      annotations = modifiers.annotations.filterNot(a => {
        M.isAnnotation(a, "CanFreeze") ||
        M.isAnnotation(a, "CannotFreeze")
      })
    )
  }

  private def generateFreezeMethodBody(
    r: org.scaladebugger.macros.freeze.CanFreeze.ReturnType.ReturnType,
    freezeObjName: TermName,
    methodName: TermName,
    returnType: Tree
  ): Tree = {
    import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
    r match {
      case ReturnType.Normal =>
        q"""scala.util.Try.apply($freezeObjName.$methodName)
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeObject =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.freeze())
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeCollection =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.map(_.freeze()))
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeOption =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.map(_.freeze()))
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeEither =>
        q"""
          scala.util.Try.apply($freezeObjName.$methodName).map(r => r match {
            case scala.util.Left(o) => scala.util.Left(o.freeze())
            case scala.util.Right(o) => scala.util.Right(o.freeze())
          }).asInstanceOf[scala.util.Try[$returnType]]
        """
    }
  }

  private def generateFreezeMethodBody(
    r: org.scaladebugger.macros.freeze.CanFreeze.ReturnType.ReturnType,
    freezeObjName: TermName,
    methodName: TermName,
    returnType: TypeName
  ): Tree = {
    import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
    r match {
      case ReturnType.Normal =>
        q"""scala.util.Try.apply($freezeObjName.$methodName)
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeObject =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.freeze())
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeCollection =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.map(_.freeze()))
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeOption =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.map(_.freeze()))
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeEither =>
        q"""
          scala.util.Try.apply($freezeObjName.$methodName).map(r => r match {
            case scala.util.Left(o) => scala.util.Left(o.freeze())
            case scala.util.Right(o) => scala.util.Right(o.freeze())
          }).asInstanceOf[scala.util.Try[$returnType]]
        """
    }
  }

  private def generateTrees(
    body: List[Tree],
    traitTypeName: TypeName,
    parentTypes: List[Type],
    inheritedMethods: List[MethodSymbol]
  ): List[Tree] = {
    val freezeObjName = TermName("valueToFreeze")

    val internalMethodTrees = body.collect { case d: DefDef => d }
    val internalGroups = internalMethodTrees.groupBy(d => {
      if (M.containsAnnotation(d, "CanFreeze")) "CanFreeze"
      else if (M.containsAnnotation(d, "CannotFreeze")) "CannotFreeze"
      else "Other"
    })
    val freezableMethods = internalGroups.getOrElse("CanFreeze", Nil)
    val unfreezableMethods = internalGroups.getOrElse("CannotFreeze", Nil)
    val otherMethods = internalGroups.getOrElse("Other", Nil)

    // Validate that we don't have a method left behind without an implementation
    otherMethods.foreach {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        if (expr.isEmpty) c.abort(
          c.enclosingPosition,
          s"$tname has no implementation and is not marked as @CanFreeze or @CannotFreeze!"
        )
    }

    // TODO: Figure out why multiple of method symbol show up here,
    //       resulting in us using distinct
    val filteredInheritedMethods = inheritedMethods.distinct.filterNot(m => {
      internalMethodTrees.exists(t =>
        t.name.decodedName.toString == m.name.decodedName.toString &&
        t.vparamss.size == m.paramLists.size &&
        t.vparamss.zip(m.paramLists).forall { case (valDefList, paramList) =>
          valDefList.size == paramList.size &&
          valDefList.zip(paramList).forall { case (valDef, param) =>
            // TODO: Verify this actually works -- can we encounter
            //       a scenario where tpt is not the full name?
            valDef.tpt.toString() == param.typeSignature.toString
          }
        }
      )
    })
    val iFreezableMethods = filteredInheritedMethods.filter(
      _.annotations.exists(_.tree.tpe =:= typeOf[CanFreeze])
    )

    // Build up constructor parameters for frozen class
    val cParams = freezableMethods.map {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val name = TermName(s"$$$tname")
        q"protected val $name: scala.util.Try[$tpt]"
    } ++ iFreezableMethods.map(m => {
      val name = TermName(s"$$${m.name.decodedName.toString}")

      // TODO: This is a hack to get around <error> appearing when method
      //       returns type of trait we are currently inspecting
      val tpt =
        if (m.returnType.toString == "<error>") traitTypeName
        else TypeName(m.returnType.toString)

      q"protected val $name: scala.util.Try[$tpt]"
    })

    // TODO: Remove runtime checks by getting better understanding of
    //       return type, if it contains a freeze method, etc.
    //
    // Build up constructor arguments for freeze method instantiating
    // the frozen class
    val cArgs = freezableMethods.map { d =>
      val q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" = d

      // TODO: Find better way to match against the input
      val r = M.findAnnotation(d, "CanFreeze").get match {
        case q"new CanFreeze()" =>
          import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
          ReturnType.Normal
        case q"new CanFreeze($returnType)" =>
          import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
          ReturnType.withName(returnType.toString().split('.').last)
      }

      generateFreezeMethodBody(r, freezeObjName, tname, tpt)
    } ++ iFreezableMethods.map(m => {
      val tname = TermName(m.name.decodedName.toString)

      // TODO: This is a hack to get around <error> appearing when method
      //       returns type of trait we are currently inspecting
      val tpt =
        if (m.returnType.toString == "<error>") traitTypeName
        else TypeName(m.returnType.toString)

      val a = m.annotations.find(_.tree.tpe =:= typeOf[CanFreeze])
      if (a.isEmpty) c.abort(
        c.enclosingPosition,
        s"Invalid state while processing inherited method ${m.name}"
      )

      // TODO: Figure out better way to handle this than some random hack
      import org.scaladebugger.macros.freeze.CanFreeze.ReturnType
      val fullArgs = a.get.tree.children.map(_.toString())
      val r = scala.util.Try(
        ReturnType.withName(fullArgs.last.split('.').last)
      ).getOrElse(ReturnType.Normal)

      generateFreezeMethodBody(r, freezeObjName, tname, tpt)
    })

    val newFreezableMethods = freezableMethods.map {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val name = TermName(s"$$$tname")

        if (paramss.nonEmpty) c.abort(
          c.enclosingPosition,
          s"Unable to freeze $tname! Must have zero arguments!"
        )

        val newMods: Modifiers = markModifiersOverride(mods match {
          case m: Modifiers => m
        })

        q"$newMods def $tname[..$tparams](...$paramss): $tpt = this.$name.get"
    }

    val newUnfreezableMethods = unfreezableMethods.map {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val newMods: Modifiers = markModifiersOverride(mods match {
          case m: Modifiers => m
        })

        q"""$newMods def $tname[..$tparams](...$paramss): $tpt =
          throw new IllegalStateException("Method not frozen!")"""
    }

    val newInheritedMethods = filteredInheritedMethods.flatMap(m => {
      val methodName = m.name.decodedName.toString
      val mods = m.annotations.map(_.tree)
      val tname = TermName(methodName)
      val tparams = m.typeParams.map(tp => TypeName(tp.fullName))
      val paramss = m.paramLists.map(_.map(p => {
        val name = TermName(p.name.decodedName.toString)
        val typeName = TypeName(p.typeSignature.toString)

        // TODO: Support default values?
        // TODO: Support annotations?
        q"val $name: $typeName"
      }))

      // TODO: This is a hack to get around <error> appearing when method
      //       returns type of trait we are currently inspecting
      val tpt =
        if (m.returnType.toString == "<error>")
          M.classNameToTree(traitTypeName.decodedName.toString)
        else
          M.classNameToTree(m.returnType.toString)

      val aFreezable = m.annotations.find(_.tree.tpe =:= typeOf[CanFreeze])
      val aUnfreezable = m.annotations.find(_.tree.tpe =:= typeOf[CannotFreeze])

      val hasAnnotation = aFreezable.nonEmpty || aUnfreezable.nonEmpty
      if (hasAnnotation && m.isFinal) c.abort(
        c.enclosingPosition,
        s"Inherited $methodName is marked as final and cannot be overridden!"
      )
      if (!hasAnnotation && m.isAbstract) c.abort(
        c.enclosingPosition,
        Seq(
          s"Inherited $methodName has no implementation and",
          "is not marked as @CanFreeze or @CannotFreeze!"
        ).mkString(" ")
      )

      val methodTree = if (aFreezable.nonEmpty) {
        val name = TermName(s"$$$tname")
        q"override def $tname[..$tparams](...$paramss): $tpt = this.$name.get"
      } else if (aUnfreezable.nonEmpty) {
        q"""override def $tname[..$tparams](...$paramss): $tpt =
          throw new IllegalStateException("Method not frozen!")"""
      } else {
        null
      }

      //println("GENERATING METHOD FOR " + m.name + " OF " + m.owner.name)
      Option(methodTree)
    })

    val frozenParents = parentTypes.map(t =>
      M.classNameToTree(t.typeSymbol.fullName + ".Frozen")
    )

    val parents = frozenParents ++ List(
       typeOf[java.io.Serializable]
    ).map(t => TypeTree(t))

    val klass = q"""
      final class Frozen private[$traitTypeName](..$cParams) extends $traitTypeName {
        ..$newFreezableMethods
        ..$newUnfreezableMethods
        ..$newInheritedMethods
      }
    """

    val method = q"""
      def freeze($freezeObjName: $traitTypeName): $traitTypeName = new Frozen(..$cArgs)
    """

    object Q {
      object Y
      type T = Test
      class Test extends scala.AnyRef
      trait Pants
    }
    val x: Q.Test = null
    val y: Q.Pants = null

    List(klass, method)
  }
}

