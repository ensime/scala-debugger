package org.scaladebugger.macros

import scala.language.experimental.macros
import scala.annotation.tailrec
import scala.reflect.macros.whitebox
import macrocompat.bundle

@bundle class MacroUtils[C <: whitebox.Context](val c: C) {
  import c.universe._

  def containsAnnotation(d: DefDef, aName: String): Boolean =
    findAnnotation(d, aName).nonEmpty

  def findAnnotation(d: DefDef, aName: String): Option[Tree] =
    d.mods.annotations.find(a => isAnnotation(a, aName))

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

  def extractInheritedMethods(parents: List[Type]): List[MethodSymbol] = {
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

    // TODO: Getting paramLists and materializing seems to trigger the
    //       appearance of our static annotations; figure out why and fix
    extract(parents, Nil).map(m => {
      m.paramLists.toString
      m
    })
  }

  def extractTypes(trees: List[Tree]): List[Option[Type]] =
    trees.map(extractType)

  def extractType(tree: Tree): Option[Type] = {
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

    tree match {
      case Ident(name) if !isBaseType(name) =>
        val typeName = name.toTypeName
        val typedTree = scala.util.Try(c.typecheck(
          q"null.asInstanceOf[$typeName]",
          withMacrosDisabled = true
        ))
        typedTree.toOption.flatMap(tt => Option(tt.tpe))
      case Select(Ident(name), typeName) if !isBaseType(name, typeName) =>
        val t1 = name.toTermName
        val t2 = typeName.toTypeName
        val typedTree = scala.util.Try(c.typecheck(
          q"null.asInstanceOf[$t1.$t2]",
          withMacrosDisabled = true
        ))
        typedTree.toOption.flatMap(tt => Option(tt.tpe))
      case a =>
        None
    }
  }

  def currentPackage(): String = {
    val typeName = TypeName("Dummy" + c.freshName())
    val dummyType = c.typecheck(q"class $typeName").tpe
    dummyType.typeConstructor
    val ownerSymbol = dummyType.typeSymbol.owner
    println("TYPE SYMBOL OF " + typeName + " IS " + dummyType.typeSymbol)
    println("TERM SYMBOL OF " + typeName + " IS " + dummyType.termSymbol)
    println("OWNER OF " + typeName + " IS " + ownerSymbol)

    @tailrec def buildPackage(owner: Symbol, tokens: List[String]): String = {
      if (owner == NoSymbol) tokens.mkString(".")
      else buildPackage(owner.owner, owner.name.decodedName.toString +: tokens)
    }

    buildPackage(ownerSymbol, Nil)
  }

  def newEmptyObject(name: String): ModuleDef = {
    val oName = TermName(name)
    val moduleDef: ModuleDef = q"object $oName {}" match {
      case m: ModuleDef => m
    }
    moduleDef
  }

  def classNameToTree(fqcn: String): Tree = {
    val tokens = fqcn.split('.').filter(_.nonEmpty)

    val packageName = tokens.take(tokens.length - 1).toList
    val pTermNames = packageName.map(TermName.apply)

    val className = tokens.last
    val cTypeName = TypeName(className)

    if (tokens.length <= 1) Ident(cTypeName)
    else {
      val pRef = pTermNames.tail.foldLeft(Ident(pTermNames.head): RefTree) {
        case (s, n) => Select(s, n)
      }
      Select(pRef, cTypeName)
    }
  }
}
