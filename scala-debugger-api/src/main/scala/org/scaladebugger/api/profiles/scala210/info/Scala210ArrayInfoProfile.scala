package org.scaladebugger.api.profiles.scala210.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.PureArrayInfoProfile
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a Scala 2.10 implementation of an array profile.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param _arrayReference The reference to the underlying JDI array
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _threadReference The thread associated with the array (for method
 *                        invocation)
 * @param _referenceType The reference type for this array
 */
class Scala210ArrayInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _arrayReference: ArrayReference
)(
  override protected val _virtualMachine: VirtualMachine = _arrayReference.virtualMachine(),
  private val _threadReference: ThreadReference = _arrayReference.owningThread(),
  private val _referenceType: ReferenceType = _arrayReference.referenceType()
) extends PureArrayInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _arrayReference = _arrayReference
)(
  _virtualMachine = _virtualMachine,
  _threadReference = _threadReference,
  _referenceType = _referenceType
) {
  override protected def newValueProfile(value: Value): ValueInfoProfile =
    new Scala210ValueInfoProfile(scalaVirtualMachine, value)
}
