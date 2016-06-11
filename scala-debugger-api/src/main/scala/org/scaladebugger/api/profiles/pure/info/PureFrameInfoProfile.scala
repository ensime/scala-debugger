package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.{Failure, Try}
import scala.collection.JavaConverters._

/**
 * Represents a pure implementation of a stack frame profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            stack frame
 * @param _stackFrame The reference to the underlying JDI stack frame instance
 * @param index The index of the frame relative to the frame stack
 */
class PureFrameInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  private val _stackFrame: StackFrame,
  val index: Int
) extends FrameInfoProfile {
  private lazy val _threadReference = _stackFrame.thread()
  private lazy val _thisObjectProfile =
    Option(_stackFrame.thisObject()).map(newObjectProfile)
  private lazy val _currentThreadProfile = newThreadProfile(_threadReference)
  private lazy val _locationProfile = newLocationProfile(_stackFrame.location())

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: StackFrame = _stackFrame


  /**
   * Retrieves the object representing 'this' in the current frame scope.
   *
   * @return Some profile of this object, or None if not available
   */
  override def thisObjectOption: Option[ObjectInfoProfile] = _thisObjectProfile

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return The profile of the thread
   */
  override def currentThread: ThreadInfoProfile = _currentThreadProfile

  /**
   * Retrieves the location associated with this frame.
   *
   * @return The profile of the location
   */
  override def location: LocationInfoProfile = _locationProfile

  /**
   * Retrieves the values of the arguments in this frame. As indicated by the
   * JDI spec, this can return values when no variable information is present.
   *
   * @return The collection of argument values in order as provided to the frame
   */
  override def argumentValues: Seq[ValueInfoProfile] = {
    import scala.collection.JavaConverters._
    _stackFrame.getArgumentValues.asScala.map(newValueProfile)
  }

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  override def variableOption(name: String): Option[VariableInfoProfile] = {
    Option(_stackFrame.visibleVariableByName(name)).map(newLocalVariableProfile)
      .orElse(_thisObjectProfile.flatMap(_.fieldOption(name)))
  }

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def fieldVariables: Seq[FieldVariableInfoProfile] = {
    _thisObjectProfile.map(_.fields).getOrElse(Nil)
  }

  /**
   * Retrieves all variables in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def allVariables: Seq[VariableInfoProfile] =
    localVariables ++ fieldVariables

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def localVariables: Seq[IndexedVariableInfoProfile] = {
    _stackFrame.visibleVariables().asScala.map(newLocalVariableProfile)
  }

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def nonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = {
    localVariables.filterNot(_.isArgument)
  }

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def argumentLocalVariables: Seq[IndexedVariableInfoProfile] = {
    localVariables.filter(_.isArgument)
  }

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  override def indexedVariableOption(name: String): Option[VariableInfoProfile] = {
    indexedLocalVariables.reverse.find(_.name == name)
      .orElse(_thisObjectProfile.flatMap(_.indexedFieldOption(name)))
  }

  /**
   * Retrieves all variables that represent arguments in this frame with their
   * offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = {
    indexedLocalVariables.filter(_.isArgument)
  }

  /**
   * Retrieves all variables that represent field variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedFieldVariables: Seq[FieldVariableInfoProfile] = {
    _thisObjectProfile.map(_.indexedFields).getOrElse(Nil)
  }

  /**
   * Retrieves all variables in this frame with their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedAllVariables: Seq[VariableInfoProfile] = {
    indexedLocalVariables ++ indexedFieldVariables
  }

  /**
   * Retrieves all variables that do not represent arguments in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = {
    indexedLocalVariables.filterNot(_.isArgument)
  }

  /**
   * Retrieves all variables that represent local variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedLocalVariables: Seq[IndexedVariableInfoProfile] = {
    _stackFrame.visibleVariables().asScala.zipWithIndex.map { case (v, i) =>
      newLocalVariableProfile(v, i)
    }
  }

  protected def newLocalVariableProfile(
    localVariable: LocalVariable
  ): IndexedVariableInfoProfile = newLocalVariableProfile(localVariable, -1)

  protected def newLocalVariableProfile(
    localVariable: LocalVariable,
    offsetIndex: Int
  ): IndexedVariableInfoProfile = new PureLocalVariableInfoProfile(
    scalaVirtualMachine,
    this,
    localVariable,
    offsetIndex
  )()

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfoProfile =
    new PureObjectInfoProfile(scalaVirtualMachine, objectReference)(
      _threadReference = _threadReference
    )

  protected def newThreadProfile(threadReference: ThreadReference): ThreadInfoProfile =
    new PureThreadInfoProfile(scalaVirtualMachine, threadReference)()

  protected def newLocationProfile(location: Location): LocationInfoProfile =
    new PureLocationInfoProfile(scalaVirtualMachine, location)

  protected def newValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)
}
