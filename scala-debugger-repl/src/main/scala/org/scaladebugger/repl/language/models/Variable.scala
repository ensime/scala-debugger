package org.scaladebugger.repl.language.models

case class Variable(
  identifier: Identifier,
  value: Expression
) extends Expression
