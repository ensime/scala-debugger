package org.scaladebugger.macros.freeze

import scala.reflect.macros.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.internal.annotations.compileTimeOnly

@compileTimeOnly("enable macro paradise to expand macro annotations")
class Freezable extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro FreezableMacro
}

object FreezableMacro {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._
    val inputs = annottees.map(_.tree).toList
    val (annottee, expandees) = inputs match {
      case (param: ClassDef) :: (rest @ (_ :: _)) => (param, rest)
      case (param: DefDef) :: (rest @ (_ :: _)) => (param, rest)
      case (param: ValDef) :: (rest @ (_ :: _)) => (param, rest)
      case (param: TypeDef) :: (rest @ (_ :: _)) => (param, rest)
      case _ => (EmptyTree, inputs)
    }
    println((annottee, expandees))
    val outputs = expandees
    c.Expr[Any](Block(outputs, Literal(Constant(()))))
  }
}
