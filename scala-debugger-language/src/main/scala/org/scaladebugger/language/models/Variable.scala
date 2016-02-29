package org.scaladebugger.language.models
import acyclic.file

case class Variable(
  identifier: Identifier,
  value: Expression
) extends Expression
