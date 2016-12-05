package org.scaladebugger.api.profiles.traits.info

import com.sun.jdi.StackFrame

import scala.util.Try

/**
 * Represents the interface for frame-based interaction.
 */
trait FrameInfoProfile extends CommonInfoProfile {
  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: FrameInfoProfile

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
  def tryThisObject: Try[ObjectInfoProfile] = Try(thisObject)

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return The profile of this object
   */
  @throws[NoSuchElementException]
  def thisObject: ObjectInfoProfile = thisObjectOption.get

  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return Some profile of this object, or None if not available
   */
  def thisObjectOption: Option[ObjectInfoProfile]

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return Success containing the profile of the thread, otherwise a failure
   */
  def tryCurrentThread: Try[ThreadInfoProfile] = Try(currentThread)

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return The profile of the thread
   */
  def currentThread: ThreadInfoProfile

  /**
   * Retrieves the location associated with this frame.
   *
   * @return Success containing the profile of the location, otherwise a failure
   */
  def tryLocation: Try[LocationInfoProfile] = Try(location)

  /**
   * Retrieves the location associated with this frame.
   *
   * @return The profile of the location
   */
  def location: LocationInfoProfile

  /**
   * Retrieves the values of the arguments in this frame. As indicated by the
   * JDI spec, this can return values when no variable information is present.
   *
   * @return Success containing the collection of argument values in order as
   *         provided to the frame, otherwise a failure
   */
  def tryArgumentValues: Try[Seq[ValueInfoProfile]] = Try(argumentValues)

  /**
   * Retrieves the values of the arguments in this frame. As indicated by the
   * JDI spec, this can return values when no variable information is present.
   *
   * @return The collection of argument values in order as provided to the frame
   */
  def argumentValues: Seq[ValueInfoProfile]

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Success containing profile of the variable if found, otherwise
   *         a failure
   */
  def tryVariable(name: String): Try[VariableInfoProfile] =
    Try(variable(name))

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Profile of the variable or throws an exception
   */
  @throws[NoSuchElementException]
  def variable(name: String): VariableInfoProfile = variableOption(name).get

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  def variableOption(name: String): Option[VariableInfoProfile]

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Success containing profile of the variable if found, otherwise
   *         a failure
   */
  def tryIndexedVariable(name: String): Try[VariableInfoProfile] =
    Try(indexedVariable(name))

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Profile of the variable or throws an exception
   */
  @throws[NoSuchElementException]
  def indexedVariable(name: String): VariableInfoProfile =
    indexedVariableOption(name).get

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  def indexedVariableOption(name: String): Option[VariableInfoProfile]

  /**
   * Retrieves all variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryAllVariables: Try[Seq[VariableInfoProfile]] =
    Try(allVariables)

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] =
    Try(argumentLocalVariables)

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryNonArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] =
    Try(nonArgumentLocalVariables)

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryLocalVariables: Try[Seq[IndexedVariableInfoProfile]] =
    Try(localVariables)

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryFieldVariables: Try[Seq[FieldVariableInfoProfile]] =
    Try(fieldVariables)

  /**
   * Retrieves all variables in this frame with their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedAllVariables: Try[Seq[VariableInfoProfile]] =
    Try(indexedAllVariables)

  /**
   * Retrieves all variables that represent arguments in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] =
    Try(indexedArgumentLocalVariables)

  /**
   * Retrieves all variables that do not represent arguments in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedNonArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] =
    Try(indexedNonArgumentLocalVariables)

  /**
   * Retrieves all variables that represent local variables in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedLocalVariables: Try[Seq[IndexedVariableInfoProfile]] =
    Try(indexedLocalVariables)

  /**
   * Retrieves all variables that represent field variables in this frame with
   * their offset index information.
   *
   * @return Success containing the collection of variables as their profile
   *         equivalents, otherwise a failure
   */
  def tryIndexedFieldVariables: Try[Seq[FieldVariableInfoProfile]] =
    Try(indexedFieldVariables)

  /**
   * Retrieves all variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def allVariables: Seq[VariableInfoProfile]

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def argumentLocalVariables: Seq[IndexedVariableInfoProfile]

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def nonArgumentLocalVariables: Seq[IndexedVariableInfoProfile]

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def localVariables: Seq[IndexedVariableInfoProfile]

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @return The collection of variables as their profile equivalents
   */
  def fieldVariables: Seq[FieldVariableInfoProfile]

  /**
   * Retrieves all variables in this frame with their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedAllVariables: Seq[VariableInfoProfile]

  /**
   * Retrieves all variables that represent arguments in this frame with their
   * offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedArgumentLocalVariables: Seq[IndexedVariableInfoProfile]

  /**
   * Retrieves all variables that do not represent arguments in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfoProfile]

  /**
   * Retrieves all variables that represent local variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedLocalVariables: Seq[IndexedVariableInfoProfile]

  /**
   * Retrieves all variables that represent field variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  def indexedFieldVariables: Seq[FieldVariableInfoProfile]

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
