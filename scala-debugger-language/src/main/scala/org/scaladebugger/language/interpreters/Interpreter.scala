package org.scaladebugger.language.interpreters

import org.scaladebugger.language.models.Context

import scala.util.Try

/**
 * Represents the standard interface for an interpreter.
 */
trait Interpreter {
  /**
   * Interprets a segment code by parsing and evaluating it.
   *
   * @param code The line of code to interpret
   *
   * @return The result of parsing and evaluation, yielding the last evaluated
   *         line of code
   */
  def interpret(code: String): Try[AnyRef]

  /**
   * Interprets a segment of code by parsing and evaluating it.
   *
   * @param code The line of code to interpret
   *
   * @return The result of parsing and evaluation, yielding all evaluated lines
   *         of code
   */
  def interpretVerbosely(code: String): Try[Seq[Try[AnyRef]]]

  /**
   * Represents the contextual information about the interpreter at a given
   * point in time.
   *
   * @return The context of the interpreter
   */
  def context: Context

  /**
   * Stores into the global namespace of the interpreter the provided value,
   * creating a variable with the specified name.
   *
   * @param name The name of the variable to contain the value
   * @param value The value to store
   */
  def put(name: String, value: Any): Unit

  /**
   * Retrieves the contents of a variable from the interpreter.
   *
   * @param name The name of the variable whose contents to retrieve
   *
   * @return Some value if the variable exists, otherwise None
   */
  def get(name: String): Option[Any]

  /**
   * Binds a function into the interpreter.
   *
   * @param name The name to associate with the function
   * @param parameters The collection of parameter names used by the function
   * @param function The implementation of the function
   * @param documentation Optional documentation for the function
   */
  def bindFunction(
    name: String,
    parameters: Seq[String],
    function: (Map[String, Any]) => Any,
    documentation: String = null
  ): Unit
}
