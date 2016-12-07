package org.scaladebugger.api.profiles.pure.info

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
 * @param infoProducer The producer of info-based profile instances
 * @param _stackFrame The reference to the underlying JDI stack frame instance
 * @param index The index of the frame relative to the frame stack
 */
class PureFrameInfo(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducer,
  private val _stackFrame: StackFrame,
  val index: Int
) extends FrameInfo {
  private lazy val _threadReference = _stackFrame.thread()
  private lazy val _thisObjectProfile =
    Option(_stackFrame.thisObject()).map(newObjectProfile)
  private lazy val _currentThreadProfile = newThreadProfile(_threadReference)
  private lazy val _locationProfile = newLocationProfile(_stackFrame.location())

  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: FrameInfo = {
    infoProducer.toJavaInfo.newFrameInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      stackFrame = _stackFrame,
      offsetIndex = index
    )
  }

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
  override def thisObjectOption: Option[ObjectInfo] = _thisObjectProfile

  /**
   * Retrieves the thread associated with this frame.
   *
   * @return The profile of the thread
   */
  override def currentThread: ThreadInfo = _currentThreadProfile

  /**
   * Retrieves the location associated with this frame.
   *
   * @return The profile of the location
   */
  override def location: LocationInfo = _locationProfile

  /**
   * Retrieves the values of the arguments in this frame. As indicated by the
   * JDI spec, this can return values when no variable information is present.
   *
   * @return The collection of argument values in order as provided to the frame
   */
  override def argumentValues: Seq[ValueInfo] = {
    import scala.collection.JavaConverters._
    _stackFrame.getArgumentValues.asScala.map(newValueProfile)
  }

  /**
   * Retrieves the variable with the specified name from the frame.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  override def variableOption(name: String): Option[VariableInfo] = {
    Option(_stackFrame.visibleVariableByName(name)).map(newLocalVariableProfile)
      .orElse(_thisObjectProfile.flatMap(_.fieldOption(name)))
  }

  /**
   * Retrieves all variables that represent field variables in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def fieldVariables: Seq[FieldVariableInfo] = {
    _thisObjectProfile.map(_.fields).getOrElse(Nil)
  }

  /**
   * Retrieves all variables in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def allVariables: Seq[VariableInfo] =
    localVariables ++ fieldVariables

  /**
   * Retrieves all variables that represent local variables in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def localVariables: Seq[IndexedVariableInfo] = {
    _stackFrame.visibleVariables().asScala.map(newLocalVariableProfile)
  }

  /**
   * Retrieves all variables that do not represent arguments in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def nonArgumentLocalVariables: Seq[IndexedVariableInfo] = {
    localVariables.filterNot(_.isArgument)
  }

  /**
   * Retrieves all variables that represent arguments in this frame.
   *
   * @note Provides offset index information!
   * @return The collection of variables as their profile equivalents
   */
  override def argumentLocalVariables: Seq[IndexedVariableInfo] = {
    localVariables.filter(_.isArgument)
  }

  /**
   * Retrieves the variable with the specified name from the frame with offset
   * index information.
   *
   * @param name The name of the variable to retrieve
   * @return Some profile of the variable, or None if it doesn't exist
   */
  override def indexedVariableOption(name: String): Option[VariableInfo] = {
    indexedLocalVariables.reverse.find(_.name == name)
      .orElse(_thisObjectProfile.flatMap(_.indexedFieldOption(name)))
  }

  /**
   * Retrieves all variables that represent arguments in this frame with their
   * offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedArgumentLocalVariables: Seq[IndexedVariableInfo] = {
    indexedLocalVariables.filter(_.isArgument)
  }

  /**
   * Retrieves all variables that represent field variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedFieldVariables: Seq[FieldVariableInfo] = {
    _thisObjectProfile.map(_.indexedFields).getOrElse(Nil)
  }

  /**
   * Retrieves all variables in this frame with their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedAllVariables: Seq[VariableInfo] = {
    indexedLocalVariables ++ indexedFieldVariables
  }

  /**
   * Retrieves all variables that do not represent arguments in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfo] = {
    indexedLocalVariables.filterNot(_.isArgument)
  }

  /**
   * Retrieves all variables that represent local variables in this frame with
   * their offset index information.
   *
   * @return The collection of variables as their profile equivalents
   */
  override def indexedLocalVariables: Seq[IndexedVariableInfo] = {
    _stackFrame.visibleVariables().asScala.zipWithIndex.map { case (v, i) =>
      newLocalVariableProfile(v, i)
    }
  }

  protected def newLocalVariableProfile(
    localVariable: LocalVariable
  ): IndexedVariableInfo = newLocalVariableProfile(localVariable, -1)

  protected def newLocalVariableProfile(
    localVariable: LocalVariable,
    offsetIndex: Int
  ): IndexedVariableInfo = infoProducer.newLocalVariableInfoProfile(
    scalaVirtualMachine,
    this,
    localVariable,
    offsetIndex
  )()

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfo =
    infoProducer.newObjectInfoProfile(scalaVirtualMachine, objectReference)()

  protected def newThreadProfile(threadReference: ThreadReference): ThreadInfo =
    infoProducer.newThreadInfoProfile(scalaVirtualMachine, threadReference)()

  protected def newLocationProfile(location: Location): LocationInfo =
    infoProducer.newLocationInfoProfile(scalaVirtualMachine, location)

  protected def newValueProfile(value: Value): ValueInfo =
    infoProducer.newValueInfoProfile(scalaVirtualMachine, value)
}
