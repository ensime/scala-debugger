package org.scaladebugger.language.models

case class FunctionCall(
  expression: Expression,
  values: Seq[(Identifier, Expression)]
) extends Expression

