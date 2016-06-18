package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a local variable profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            local variable
 * @param infoProducer The producer of info-based profile instances
 * @param frame The frame associated with the local variable instance
 * @param _localVariable The reference to the underlying JDI local variable
 * @param offsetIndex The index of the offset of this variable relative to other
 *                    variables in the same stack frame (or -1 if not
 *                    providing the information)
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class PureLocalVariableInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducerProfile,
  val frame: FrameInfoProfile,
  private val _localVariable: LocalVariable,
  val offsetIndex: Int
)(
  protected val _virtualMachine: VirtualMachine = _localVariable.virtualMachine()
) extends IndexedVariableInfoProfile with PureCreateInfoProfile {
  private lazy val stackFrame = frame.toJdiInstance

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: LocalVariable = _localVariable

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = _localVariable.name()

  /**
   * Returns the name of the type representing the variable.
   *
   * @return The type name as a string
   */
  override def typeName: String = _localVariable.typeName()

  /**
   * Returns the type information for the variable.
   *
   * @return The profile containing type information
   */
  override def typeInfo: TypeInfoProfile =
    newTypeProfile(_localVariable.`type`())

  /**
   * Returns the index of the stack frame where this variable is located.
   *
   * @return The frame starting from 0 (top of the stack)
   */
  override def frameIndex: Int = frame.index

  /**
   * Returns whether or not this variable represents a field.
   *
   * @return True if a field, otherwise false
   */
  override def isField: Boolean = false

  /**
   * Returns whether or not this variable represents an argument.
   *
   * @return True if an argument, otherwise false
   */
  override def isArgument: Boolean = _localVariable.isArgument

  /**
   * Returns whether or not this variable represents a local variable.
   *
   * @return True if a local variable, otherwise false
   */
  override def isLocal: Boolean = true

  /**
   * Sets the value of this variable using info about another remote value.
   *
   * @param valueInfo The remote value to set for the variable
   * @return The info for the variable's new value
   */
  override def setValueFromInfo(
    valueInfo: ValueInfoProfile
  ): ValueInfoProfile = {
    stackFrame.setValue(_localVariable, valueInfo.toJdiInstance)
    valueInfo
  }

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  override def toValueInfo: ValueInfoProfile = newValueProfile(
    stackFrame.getValue(_localVariable)
  )

  protected def newValueProfile(value: Value): ValueInfoProfile =
    infoProducer.newValueInfoProfile(scalaVirtualMachine, value)

  protected def newTypeProfile(_type: Type): TypeInfoProfile =
    infoProducer.newTypeInfoProfile(scalaVirtualMachine, _type)
}
