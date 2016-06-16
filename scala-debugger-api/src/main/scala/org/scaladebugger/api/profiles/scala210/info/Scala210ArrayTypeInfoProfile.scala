package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.PureArrayTypeInfoProfile
import org.scaladebugger.api.profiles.traits.info.{ArrayTypeInfoProfile, _}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a Scala 2.10 implementation of a array type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array type
 * @param _arrayType The underlying JDI array type to wrap
 */
class Scala210ArrayTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _arrayType: ArrayType
) extends PureArrayTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _arrayType = _arrayType
) {
  override protected def newArrayProfile(arrayReference: ArrayReference): ArrayInfoProfile =
    new Scala210ArrayInfoProfile(scalaVirtualMachine, arrayReference)()
}
