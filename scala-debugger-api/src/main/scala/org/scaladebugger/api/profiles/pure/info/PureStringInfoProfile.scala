package org.scaladebugger.api.profiles.pure.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of an array profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param _stringReference The reference to the underlying JDI string
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _threadReference The thread associated with the string (for method
 *                        invocation)
 * @param _referenceType The reference type for this array
 */
class PureStringInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val _stringReference: StringReference
)(
  override protected val _virtualMachine: VirtualMachine = _stringReference.virtualMachine(),
  private val _threadReference: ThreadReference = _stringReference.owningThread(),
  private val _referenceType: ReferenceType = _stringReference.referenceType()
) extends PureObjectInfoProfile(scalaVirtualMachine, infoProducer, _stringReference)(
  _virtualMachine = _virtualMachine,
  _threadReference = _threadReference,
  _referenceType = _referenceType
) with StringInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: StringReference = _stringReference
}
