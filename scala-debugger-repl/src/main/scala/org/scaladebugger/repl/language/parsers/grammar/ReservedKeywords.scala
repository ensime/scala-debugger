package org.scaladebugger.repl.language.parsers.grammar

object ReservedKeywords {
  val CreateFunction = "func"
  val AssignVariable = "var"
  val If = "if"
  val Else = "else"
  val Truthy = "true"
  val Falsey = "false"

  val Values = Seq(
    Truthy,
    Falsey
  )

  val NonValues = Seq(
    CreateFunction,
    AssignVariable,
    If,
    Else
  )

  val All = Values ++ NonValues
}
