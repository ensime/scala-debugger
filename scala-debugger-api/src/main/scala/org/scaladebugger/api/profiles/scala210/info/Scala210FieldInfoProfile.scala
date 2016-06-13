package org.scaladebugger.api.profiles.scala210.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.PureFieldInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents an implementation of a field profile that adds Scala 2.10 specific
 * debug logic.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            field
 * @param _container Either the object or reference type containing the
 *                   field instance
 * @param _field The reference to the underlying JDI field
 * @param offsetIndex The index of the offset of this field relative to other
 *                    fields in the same class (or -1 if not providing the
 *                    information)
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class Scala210FieldInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val _container: Either[ObjectReference, ReferenceType],
  private val _field: Field,
  override val offsetIndex: Int
)(
  override protected val _virtualMachine: VirtualMachine = _field.virtualMachine()
) extends PureFieldInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _container = _container,
  _field = _field
)(
  _virtualMachine = _virtualMachine
) {
  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = {
    val rawName = super.name

    // Grab tail end of org$scaladebugger$class$$fieldName
    // as well as org.scaladebugger.class.fieldName
    val parsePattern = """(\w+[\$|\.])*(\w+)""".r

    val parsePattern(_, scalaName) = rawName
    scalaName
  }
}
