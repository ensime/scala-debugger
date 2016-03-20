package org.scaladebugger.language.models
import acyclic.file

sealed trait Function extends BaseValue {
  val parameters: Seq[Identifier]
  val documentation: Option[String]

  override def toScalaValue: AnyRef =
    s"Parameters: ${parameters.map(_.name).mkString(",")}"
}
sealed trait CallableFunction extends Function

/**
 * Represents a function created through the interpreter. This implementation
 * is missing the closure that will be filled in elsewhere.
 *
 * @param parameters The parameter names for the function
 * @param body The body of the interpreted function
 * @param documentation Optional documentation to associate with the function
 */
case class IncompleteInterpretedFunction(
  parameters: Seq[Identifier],
  body: Expression,
  documentation: Option[String] = None
) extends Function

/**
 * Represents a function created through the interpreter.
 *
 * @param parameters The parameter names for the function
 * @param closure The enclosing scope of the function when defined
 * @param body The body of the interpreted function
 * @param documentation Optional documentation to associate with the function
 */
case class InterpretedFunction(
  parameters: Seq[Identifier],
  closure: Scope,
  body: Expression,
  documentation: Option[String] = None
) extends CallableFunction {
  override def toScalaValue: AnyRef = "<INTERPRETED> Function | " + super.toScalaValue
}

/**
 * Creates a function created outside of the interpreter.
 *
 * @param parameters The parameter names for the function
 * @param implementation The function implementation
 * @param documentation Optional documentation to associate with the function
 */
case class NativeFunction(
  parameters: Seq[Identifier],
  implementation: (Map[Identifier, Expression], Scope) => Expression,
  documentation: Option[String] = None
) extends CallableFunction {
  override def toScalaValue: AnyRef = "<NATIVE> Function | " + super.toScalaValue
}
