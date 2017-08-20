package org.scaladebugger.macros.freeze

import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import macrocompat.bundle
import org.scaladebugger.macros.MacroUtils

@bundle class FreezableMacro(val c: whitebox.Context) {
  import c.universe._

  val InternalConstructorName = "$init$"
  val FreezeMethodName = TermName("freeze")
  val FrozenTraitName = TypeName("Frozen")
  val FrozenClassName = TypeName("FrozenImpl")

  val M = new MacroUtils[c.type](c)

  // NOTE: Stuck with c.Expr[Any] instead of c.Tree for now;
  //       blows up in Scala 2.10 even with @bundle otherwise
  def impl(annottees: c.Expr[Any]*): c.Expr[Any] = {
    val id = java.util.UUID.randomUUID().toString
    val (annottee, expandees) = annottees.map(_.tree) match {
      case (classDef: ClassDef) :: (moduleDef: ModuleDef) :: Nil if M.isTrait(classDef) =>
        val results = processTraitAndObj(classDef, moduleDef)
        (EmptyTree, results)
      case (classDef: ClassDef) :: Nil if M.isTrait(classDef) =>
        val results = processTrait(classDef)
        (EmptyTree, results)
      case _ =>
        c.abort(
          c.enclosingPosition,
          "Freezable can only be placed on traits!"
        )
    }

    val outputs = expandees
    println(outputs)
    //println(showRaw(q"val x: scala.util.Try[Seq[java.lang.String]]"))
    //println(showRaw(q"val x: scala.util.Try[Seq[_]]"))

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

      // TODO: Find better way to know when to mark as overridden
      val o = TermName(tpname.toString())
      val freezeMethod =
        if (inheritedMethods.exists(_.name.toString == FreezeMethodName.toString))
          q"override def $FreezeMethodName(): $tpname = $o.$FreezeMethodName(this)"
        else
          q"def $FreezeMethodName(): $tpname = $o.$FreezeMethodName(this)"

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
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.$FreezeMethodName())
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeCollection =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.map(_.$FreezeMethodName()))
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeOption =>
        q"""scala.util.Try.apply($freezeObjName.$methodName).map(_.map(_.$FreezeMethodName()))
          .asInstanceOf[scala.util.Try[$returnType]]"""
      case ReturnType.FreezeEither =>
        q"""
          scala.util.Try.apply($freezeObjName.$methodName).map(r => r match {
            case scala.util.Left(o) => scala.util.Left(o.$FreezeMethodName())
            case scala.util.Right(o) => scala.util.Right(o.$FreezeMethodName())
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
    val internalGroups = internalMethodTrees.filterNot {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val name = tname.toString()
        name == InternalConstructorName.toString
    }.groupBy(d => {
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
      m.name.decodedName.toString == InternalConstructorName.toString ||
      m.name.decodedName.toString == FreezeMethodName.toString ||
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
    val iOtherMethods = filteredInheritedMethods.filterNot(
      _.annotations.map(_.tree.tpe).exists(t =>
        t =:= typeOf[CanFreeze] || t =:= typeOf[CannotFreeze])
    )

    // Build up dependencies for frozen trait
    val cDeps = freezableMethods.map {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val name = TermName(s"$$$tname")
        q"protected val $name: scala.util.Try[$tpt]"
    } ++ iFreezableMethods.map(m => {
      val name = TermName(s"$$${m.name.decodedName.toString}")

      val retTypeStr = m.returnType.toString
      val tpt =
        if (retTypeStr == "<error>" || retTypeStr == "...") {
          tq""
        } else {
          M.classNameToTree(retTypeStr)
        }

      q"protected val $name: scala.util.Try[$tpt]"
    }) ++ otherMethods.flatMap {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val name = TermName(s"$$$tname")

        if (paramss.nonEmpty) None
        else Some(q"override protected val $name: scala.util.Try[$tpt] = scala.util.Try.apply(this.$tname)")
    } ++ iOtherMethods.flatMap(m => {
      val mname = TermName(m.name.decodedName.toString)
      val name = TermName(s"$$${m.name.decodedName.toString}")

      val retTypeStr = m.returnType.toString
      val tpt =
        if (retTypeStr == "<error>" || retTypeStr == "...") {
          tq""
        } else {
          M.classNameToTree(retTypeStr)
        }

      if (m.paramLists.nonEmpty) None
      else Some(q"override protected val $name: scala.util.Try[$tpt] = scala.util.Try.apply(this.$mname)")

    })

    // Build up constructor parameters for frozen class
    val cParams = freezableMethods.map {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val name = TermName(s"$$$tname")
        q"protected val $name: scala.util.Try[$tpt]"
    } ++ iFreezableMethods.map(m => {
      val name = TermName(s"$$${m.name.decodedName.toString}")

      val retTypeStr = m.returnType.toString
      val tpt =
        if (retTypeStr == "<error>" || retTypeStr == "...") {
          tq""
        } else {
          M.classNameToTree(retTypeStr)
        }

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

      val retTypeStr = m.returnType.toString
      val tpt =
        if (retTypeStr == "<error>" || retTypeStr == "...") {
          tq""
        } else {
          M.classNameToTree(retTypeStr)
        }

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

    val newSuperMethods = otherMethods.map {
      case q"$mods def $tname[..$tparams](...$paramss): $tpt = $expr" =>
        val newMods: Modifiers = markModifiersOverride(mods match {
          case m: Modifiers => m
        })

        q"""$newMods def $tname[..$tparams](...$paramss): $tpt =
          super.$tname[..$tparams](...$paramss)"""
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

      val retTypeStr = m.returnType.toString
      val tpt =
        if (retTypeStr == "<error>" || retTypeStr == "...") {
          tq""
        } else {
          M.classNameToTree(retTypeStr)
        }

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

      Option(methodTree)
    })

    val parents = parentTypes
      .map(_.typeSymbol.fullName + "." + FrozenTraitName)
      .map(M.classNameToTree)

    val interface = q"""
      trait $FrozenTraitName extends $traitTypeName with ..$parents {
        ..$cDeps
        ..$newFreezableMethods
        ..$newUnfreezableMethods
      }
    """

    val klass = q"""
      final class $FrozenClassName private[$traitTypeName](..$cParams) extends $FrozenTraitName
    """

    val method = q"""
      def $FreezeMethodName($freezeObjName: $traitTypeName): $traitTypeName = new $FrozenClassName(..$cArgs)
    """

    List(interface, klass, method)
  }
}

