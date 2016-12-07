package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.api.profiles.pure.info.PureLocalVariableInfoProfile

/**
 * Represents an implementation of a local variable profile that adds Scala
 * 2.10 specific debug logic.
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
class Scala210LocalVariableInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  override val frame: FrameInfoProfile,
  private val _localVariable: LocalVariable,
  override val offsetIndex: Int
)(
  override protected val _virtualMachine: VirtualMachine =
    _localVariable.virtualMachine()
) extends PureLocalVariableInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  frame = frame,
  _localVariable = _localVariable,
  offsetIndex = offsetIndex
)(
  _virtualMachine = _virtualMachine
) {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = false

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = {
    val rawName = super.name.trim

    Rules.extractName(rawName)
  }
}
