package org.scaladebugger.api.lowlevel.wrappers

import org.scaladebugger.api.utils.Logging
import com.sun.jdi._

import scala.collection.JavaConverters._
import scala.util.{ Failure, Success, Try }

/**
 * Represents a wrapper around a stack frame, providing additional methods.
 *
 * @param _stackFrame The stack frame to wrap
 */
class StackFrameWrapper(private val _stackFrame: StackFrame) extends Logging {
  require(_stackFrame != null, "Stack frame cannot be null!")

  /**
   * Attempts to retrieve the "this" object for the underlying stack frame.
   *
   * @return Some object reference if retrieved, otherwise None
   */
  def thisObjectAsOption: Option[ObjectReference] =
    Try(_stackFrame.thisObject()).toOption

  /**
   * Extracts the value out of the try, or returns null if not available.
   *
   * @param value The value (wrapped in a Try) to extract
   *
   * @return The value instance if available, otherwise null
   */
  private def extractValue(value: Try[Value]): Value = value match {
    // Successfully retrieved local variable value, so just return it
    case Success(v) => v

    // Failed to retrieve value of local variable, so log and return null
    case Failure(ex) =>
      logger.throwable(ex)
      null
  }

  /**
   * Retrieves the specified local variable and its value for the underlying
   * stack frame.
   *
   * @param name The name of the local variable to retrieve
   *
   * @return Some tuple of local variable and value if available, otherwise None
   */
  def localVisibleVariable(name: String): Option[(LocalVariable, Value)] =
    Try(_stackFrame.visibleVariableByName(name)).toOption.map(variable =>
      variable -> extractValue(Try(_stackFrame.getValue(variable)))
    )

  /**
   * Retrieves local variables and their values for the underlying stack frame.
   *
   * @return The map of local variables paired with their respective values
   */
  def localVisibleVariableMap(): Map[LocalVariable, Value] =
    Try(_stackFrame.visibleVariables()).map(_.asScala).getOrElse(Nil)
      .map(variable =>
        variable -> extractValue(Try(_stackFrame.getValue(variable)))
      ).toMap

  /**
   * Retrieves local variables that are arguments and their values for the
   * underlying stack frame.
   *
   * @return The map of local variables paired with their respective values
   */
  def localArgumentMap(): Map[LocalVariable, Value] =
    localVisibleVariableMap().filterKeys(_.isArgument)

  /**
   * Retrieves local variables that are not arguments and their values for the
   * underlying stack frame.
   *
   * @return The map of local variables paired with their respective values
   */
  def localNonArgumentMap(): Map[LocalVariable, Value] =
    localVisibleVariableMap().filterKeys(k => !k.isArgument)

  /**
   * Retrieves the specified local variable and its value for the underlying
   * stack frame.
   *
   * @param name The name of the local variable to retrieve
   *
   * @return Some tuple of local variable and value if available, otherwise None
   */
  def thisVisibleField(name: String): Option[(Field, Value)] = {
    val stackThisObject = thisObjectAsOption.map(_.referenceType())

    stackThisObject.flatMap(stackThisObject =>
      Try(stackThisObject.fieldByName(name)).toOption
    ).map(field =>
      (field, Try(stackThisObject.get.getValue(field)).getOrElse(null))
    )
  }

  /**
   * Retrieves fields and values for the "this" object contained in the
   * underlying stack frame.
   *
   * @return The map of fields paired with their respective values
   */
  def thisVisibleFieldMap(): Map[Field, Value] = {
    val stackThisObject = thisObjectAsOption match {
      case Some(obj) => obj

      // Failed to get "this" object, so exit early
      case _ => return Map()
    }

    // Attempt to retrieve all visible fields and build a map of field -> value
    Try(stackThisObject.referenceType().visibleFields())
      .map(_.asScala).getOrElse(Nil)
      .map(field => field -> extractValue(Try(stackThisObject.getValue(field))))
      .toMap
  }
}
