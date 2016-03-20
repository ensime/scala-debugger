package org.scaladebugger.language.parsers.grammar
import acyclic.file

object ReservedKeywords {
  val CreateFunction = "func"
  val AssignVariable = "var"
  val If = "if"
  val Else = "else"
  val Truthy = "true"
  val Falsey = "false"
  val Undefined = "undefined"
  val SkipEval = "@"

  val Values = Seq(
    Truthy,
    Falsey,
    Undefined
  )

  val NonValues = Seq(
    CreateFunction,
    AssignVariable,
    If,
    Else,
    SkipEval
  )

  val All = Values ++ NonValues
}
