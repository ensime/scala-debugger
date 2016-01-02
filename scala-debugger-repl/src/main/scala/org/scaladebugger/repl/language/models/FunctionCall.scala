package org.scaladebugger.repl.language.models

case class FunctionCall(
  identifier: Identifier,
  values: Seq[(Identifier, Expression)]
) extends Expression

