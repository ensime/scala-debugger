package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a java implementation of a field profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            field
 * @param infoProducer The producer of info-based profile instances
 * @param _container Either the object or reference type containing the
 *                   field instance
 * @param _field The reference to the underlying JDI field
 * @param offsetIndex The index of the offset of this field relative to other
 *                    fields in the same class (or -1 if not providing the
 *                    information)
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 */
class JavaFieldInfo(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducer,
  private val _container: Either[ObjectReference, ReferenceType],
  private val _field: Field,
  val offsetIndex: Int
)(
  protected val _virtualMachine: VirtualMachine = _field.virtualMachine()
) extends FieldVariableInfo with JavaCreateInfoProfile {
  private lazy val _parent = _container match {
    case Left(o) => Left(newObjectProfile(o))
    case Right(r) => Right(newReferenceTypeProfile(r))
  }

  /**
   * Creates a new, java field information profile with no offset index.
   *
   * @param scalaVirtualMachine The high-level virtual machine containing the
   *                            field
   * @param infoProducer The producer of info-based profile instances
   * @param _container Either the object or reference type containing the
   *                   field instance
   * @param _field The reference to the underlying JDI field
   * @param _virtualMachine The virtual machine used to mirror local values on
   *                       the remote JVM
   */
  def this(
    scalaVirtualMachine: ScalaVirtualMachine,
    infoProducer: InfoProducer,
    _container: Either[ObjectReference, ReferenceType],
    _field: Field
  )(
    _virtualMachine: VirtualMachine
  ) = this(
    scalaVirtualMachine,
    infoProducer,
    _container,
    _field,
    -1
  )(_virtualMachine)

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
  override def toJavaInfo: FieldVariableInfo = {
    infoProducer.toJavaInfo.newFieldInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      container = _container,
      field = _field,
      offsetIndex = offsetIndex
    )(
      virtualMachine = _virtualMachine
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Field = _field

  /**
   * Returns the name of the variable.
   *
   * @return The name of the variable
   */
  override def name: String = _field.name()

  /**
   * Returns the name of the type representing the variable.
   *
   * @return The type name as a string
   */
  override def typeName: String = _field.typeName()

  /**
   * Returns the type information for the variable.
   *
   * @return The profile containing type information
   */
  override def `type`: TypeInfo = newTypeProfile(_field.`type`())

  /**
   * Returns the parent that contains this field.
   *
   * @return The reference type information (if a static field) or object
   *         information (if a non-static field)
   */
  override def parent: Either[ObjectInfo, ReferenceTypeInfo] =
    _parent

  /**
   * Returns the type where this field was declared.
   *
   * @return The reference type information that declared this field
   */
  override def declaringType: ReferenceTypeInfo =
    newReferenceTypeProfile(_field.declaringType())

  /**
   * Returns whether or not this variable represents a field.
   *
   * @return True if a field, otherwise false
   */
  override def isField: Boolean = true

  /**
   * Returns whether or not this variable represents an argument.
   *
   * @return True if an argument, otherwise false
   */
  override def isArgument: Boolean = false

  /**
   * Returns whether or not this variable represents a local variable.
   *
   * @return True if a local variable, otherwise false
   */
  override def isLocal: Boolean = false

  /**
   * Sets the value of this variable using info about another remote value.
   *
   * @param valueInfo The remote value to set for the variable
   * @return The info for the variable's new value
   */
  override def setValueFromInfo(
    valueInfo: ValueInfo
  ): ValueInfo = {
    setFieldValue(valueInfo.toJdiInstance)
    valueInfo
  }

  private def setFieldValue(value: Value): Unit = _container match {
    case Left(_objectReference) => _objectReference.setValue(_field, value)
    case Right(_classType: ClassType) => _classType.setValue(_field, value)
    case Right(other) =>
      throw new Exception(s"Cannot set value for non-class ${other.name()}!")
  }

  /**
   * Returns a profile representing the value of this variable.
   *
   * @return The profile representing the value
   */
  override def toValueInfo: ValueInfo = newValueProfile(_container match {
    case Left(_objectReference) => _objectReference.getValue(_field)
    case Right(_referenceType)  => _referenceType.getValue(_field)
  })

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfo =
    infoProducer.newObjectInfo(scalaVirtualMachine, objectReference)()

  protected def newReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfo = infoProducer.newReferenceTypeInfo(
    scalaVirtualMachine,
    referenceType
  )

  protected def newValueProfile(value: Value): ValueInfo =
    infoProducer.newValueInfo(scalaVirtualMachine, value)

  protected def newTypeProfile(_type: Type): TypeInfo =
    infoProducer.newTypeInfo(scalaVirtualMachine, _type)
}
