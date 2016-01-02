package org.scaladebugger.repl.language.models

sealed trait Function extends BaseValue {
  val parameters: Seq[Identifier]

  override def toScalaValue: AnyRef =
    s"Parameters: ${parameters.map(_.name).mkString(",")}"
}

case class InterpretedFunction(
  parameters: Seq[Identifier],
  body: Expression
) extends Function {
  override def toScalaValue: AnyRef = "<INTERPRETED> Function | " + super.toScalaValue
}

case class NativeFunction(
  parameters: Seq[Identifier],
  implementation: (Map[Identifier, Expression]) => Expression
) extends Function {
  override def toScalaValue: AnyRef = "<NATIVE> Function | " + super.toScalaValue
}
