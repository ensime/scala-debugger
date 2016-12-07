package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.StackFrame

import scala.util.Try

/**
 * Represents the interface for frame-based interaction.
 */
trait FrameInfo extends CommonInfo {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: FrameInfo

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: StackFrame

  /**
   * Returns the index of this frame relative to the frame stack.
   *
   * @return The index with 0 being the top frame
   */
  def index: Int

  /**
   * Returns whether or not this frame was provided with its index.
   *
   * @return True if an index exists, otherwise false
   */
  def hasIndex: Boolean = index >= 0

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return Success containing the profile of this object, otherwise a failure
   */
  def tryThisObject: Try[ObjectInfo] = Try(thisObject)

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return The profile of this object
   */
  @throws[NoSuchElementException]
  def thisObject: ObjectInfo = thisObjectOption.get

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return Some profile of this object, or None if not available
   */
  def thisObjectOption: Option[ObjectInfo]

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return Success containing the profile of the thread, otherwise a failure
   */
  def tryCurrentThread: Try[ThreadInfo] = Try(currentThread)

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return The profile of the thread
   */
  def currentThread: ThreadInfo

  /**
   * Retrieves the location associated with this frame.
   *
   * @return Success containing the profile of the location, otherwise a failure
   */
  def tryLocation: Try[LocationInfo] = Try(location)

  /**
   * Retrieves the location associated with this frame.
   *
   * @return The profile of the location
   */
  def location: LocationInfo

  /**
   * Retrieves the values of the arguments in this frame. As indicated by the
   * JDI spec, this can return values when no variable information is present.
   *
   * @return Success containing the collection of argument values in order as
   *         provided to the frame, otherwise a failure
   */
  def tryArgumentValues: Try[Seq[ValueInfo]] = Try(argumentValues)

  /**
   * Retrieves the values of the arguments in this frame. As indicated by the
   * JDI spec, this can return values when no variable information is present.
   *
   * @return The collection of argument values in order as provided to the frame
   */
  def argumentValues: Seq[ValueInfo]

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Success containing profile of the variable if found, otherwise
   *         a failure
   */
  def tryVariable(name: String): Try[VariableInfo] =
    Try(variable(name))

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Profile of the variable or throws an exception
   */
  @throws[NoSuchElementException]
  def variable(name: String): VariableInfo = variableOption(name).get

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  def variableOption(name: String): Option[VariableInfo]

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Success containing profile of the variable if found, otherwise
   *         a failure
   */
  def tryIndexedVariable(name: String): Try[VariableInfo] =
    Try(indexedVariable(name))

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Profile of the variable or throws an exception
   */
  @throws[NoSuchElementException]
  def indexedVariable(name: String): VariableInfo =
    indexedVariableOption(name).get

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  def indexedVariableOption(name: String): Option[VariableInfo]

  /**
   * Retrieves all variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryAllVariables: Try[Seq[VariableInfo]] =
    Try(allVariables)

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryArgumentLocalVariables: Try[Seq[IndexedVariableInfo]] =
    Try(argumentLocalVariables)

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryNonArgumentLocalVariables: Try[Seq[IndexedVariableInfo]] =
    Try(nonArgumentLocalVariables)

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryLocalVariables: Try[Seq[IndexedVariableInfo]] =
    Try(localVariables)

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryFieldVariables: Try[Seq[FieldVariableInfo]] =
    Try(fieldVariables)

  /**
   * Retrieves all variables in this frame with their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedAllVariables: Try[Seq[VariableInfo]] =
    Try(indexedAllVariables)

  /**
   * Retrieves all variables that represent arguments in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedArgumentLocalVariables: Try[Seq[IndexedVariableInfo]] =
    Try(indexedArgumentLocalVariables)

  /**
   * Retrieves all variables that do not represent arguments in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedNonArgumentLocalVariables: Try[Seq[IndexedVariableInfo]] =
    Try(indexedNonArgumentLocalVariables)

  /**
   * Retrieves all variables that represent local variables in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedLocalVariables: Try[Seq[IndexedVariableInfo]] =
    Try(indexedLocalVariables)

  /**
   * Retrieves all variables that represent field variables in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedFieldVariables: Try[Seq[FieldVariableInfo]] =
    Try(indexedFieldVariables)

  /**
   * Retrieves all variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def allVariables: Seq[VariableInfo]

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def argumentLocalVariables: Seq[IndexedVariableInfo]

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def nonArgumentLocalVariables: Seq[IndexedVariableInfo]

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def localVariables: Seq[IndexedVariableInfo]

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def fieldVariables: Seq[FieldVariableInfo]

  /**
   * Retrieves all variables in this frame with their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedAllVariables: Seq[VariableInfo]

  /**
   * Retrieves all variables that represent arguments in this frame with their
   * offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedArgumentLocalVariables: Seq[IndexedVariableInfo]

  /**
   * Retrieves all variables that do not represent arguments in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfo]

  /**
   * Retrieves all variables that represent local variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedLocalVariables: Seq[IndexedVariableInfo]

  /**
   * Retrieves all variables that represent field variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedFieldVariables: Seq[FieldVariableInfo]

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = {
    val loc = this.tryLocation.map(_.toPrettyString).getOrElse("???")
    s"Frame $index at ($loc)"
  }
}
