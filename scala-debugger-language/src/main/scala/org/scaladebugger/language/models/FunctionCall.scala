package org.scaladebugger.language.models
import acyclic.file

case class FunctionCall(
  expression: Expression,
  values: Seq[(Identifier, Expression)]
) extends Expression

