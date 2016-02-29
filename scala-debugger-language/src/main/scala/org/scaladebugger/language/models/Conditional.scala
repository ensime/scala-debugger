package org.scaladebugger.language.models
import acyclic.file

case class Conditional(
  condition: Expression,
  trueBranch: Expression,
  falseBranch: Expression
) extends Expression
