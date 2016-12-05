package org.scaladebugger.language.models

case class Conditional(
  condition: Expression,
  trueBranch: Expression,
  falseBranch: Expression
) extends Expression
