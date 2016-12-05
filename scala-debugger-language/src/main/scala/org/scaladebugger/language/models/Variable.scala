package org.scaladebugger.language.models

case class Variable(
  identifier: Identifier,
  value: Expression
) extends Expression
