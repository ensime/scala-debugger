package org.scaladebugger.api.profiles.scala210.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.pure.info.PureFieldInfoProfile
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ReferenceTypeInfoProfile, TypeInfoProfile, ValueInfoProfile}
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
   * Creates a new Scala 2.10 field information profile with no offset index.
   *
   * @param scalaVirtualMachine The high-level virtual machine containing the
   *                            field
   * @param _container Either the object or reference type containing the
   *                   field instance
   * @param _field The reference to the underlying JDI field
   * @param _virtualMachine The virtual machine used to mirror local values on
   *                       the remote JVM
   */
  def this(
    scalaVirtualMachine: ScalaVirtualMachine,
    _container: Either[ObjectReference, ReferenceType],
    _field: Field
  )(
    _virtualMachine: VirtualMachine
  ) = this(scalaVirtualMachine, _container, _field, -1)(_virtualMachine)

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = {
    val rawName = super.name

    // Grab tail end of org$scaladebugger$class$$fieldName
    // as well as org.scaladebugger.class.fieldName
    val parsePattern = """(\w+[\$|\.]+)*(\w+)""".r

    rawName match {
      case parsePattern(_, scalaName) => scalaName
      case _                          => rawName
    }
  }

  override protected def newObjectProfile(objectReference: ObjectReference): ObjectInfoProfile =
    new Scala210ObjectInfoProfile(scalaVirtualMachine, objectReference)()

  override protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = new Scala210ReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )

  override protected def newValueProfile(value: Value): ValueInfoProfile =
    new Scala210ValueInfoProfile(scalaVirtualMachine, value)

  override protected def newTypeProfile(_type: Type): TypeInfoProfile =
    new Scala210TypeInfoProfile(scalaVirtualMachine, _type)
}
