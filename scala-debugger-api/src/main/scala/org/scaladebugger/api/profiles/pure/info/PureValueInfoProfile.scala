package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

object PureValueInfoProfile {
  val DefaultNullTypeName = "null"
}

/**
 * Represents a pure implementation of a value profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            value
 * @param infoProducer The producer of info-based profile instances
 * @param _value The reference to the underlying JDI value
 */
class PureValueInfoProfile(
  val scalaVirtualMachine: ScalaVirtualMachine,
  protected val infoProducer: InfoProducerProfile,
  private val _value: Value
) extends ValueInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: Value = _value

  /**
   * Returns the type information for the value.
   *
   * @return The profile containing type information
   */
  override def typeInfo: TypeInfoProfile =
    newTypeProfile(if (!isNull) _value.`type`() else null)

  /**
   * Returns the value as an array (profile).
   *
   * @return The array profile wrapping this value
   */
  @throws[AssertionError]
  override def toArrayInfo: ArrayInfoProfile = {
    assert(isArray, "Value must be an array!")
    newArrayProfile(_value.asInstanceOf[ArrayReference])
  }

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  override def toLocalValue: Any = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    if (!isNull) _value.value()
    else null
  }

  /**
   * Returns the value as an object (profile).
   *
   * @return The object profile wrapping this value
   */
  @throws[AssertionError]
  override def toObjectInfo: ObjectInfoProfile = {
    assert(isObject, "Value must be an object!")
    newObjectProfile(_value.asInstanceOf[ObjectReference])
  }

  /**
   * Returns the value as a string (profile).
   *
   * @return The string profile wrapping this value
   */
  @throws[AssertionError]
  override def toStringInfo: StringInfoProfile = {
    assert(isString, "Value must be a string!")
    newStringProfile(_value.asInstanceOf[StringReference])
  }

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  @throws[AssertionError]
  override def toPrimitiveInfo: PrimitiveInfoProfile = {
    assert(isPrimitive, "Value must be a primitive!")
    _value match {
      case p: PrimitiveValue => newPrimitiveProfile(p)
      case v: VoidValue      => newPrimitiveProfile(v)
    }
  }

  /**
   * Returns whether or not this value represents a primitive.
   *
   * @return True if a primitive, otherwise false
   */
  override def isPrimitive: Boolean =
    !isNull && (_value.isInstanceOf[PrimitiveValue] || isVoid)

  /**
   * Returns whether or not this value represents an array.
   *
   * @return True if an array, otherwise false
   */
  override def isArray: Boolean =
    !isNull && _value.isInstanceOf[ArrayReference]

  /**
   * Returns whether or not this value represents an object.
   *
   * @return True if an object, otherwise false
   */
  override def isObject: Boolean =
    !isNull && _value.isInstanceOf[ObjectReference]

  /**
   * Returns whether or not this value represents a string.
   *
   * @return True if a string, otherwise false
   */
  override def isString: Boolean =
    !isNull && _value.isInstanceOf[StringReference]

  /**
   * Returns whether or not this value is void.
   *
   * @return True if void, otherwise false
   */
  override def isVoid: Boolean = !isNull && _value.isInstanceOf[VoidValue]

  /**
   * Returns whether or not this value is null.
   *
   * @return True if null, otherwise false
   */
  override def isNull: Boolean = _value == null

  protected def newPrimitiveProfile(primitiveValue: PrimitiveValue): PrimitiveInfoProfile =
    infoProducer.newPrimitiveInfoProfile(scalaVirtualMachine, primitiveValue)

  protected def newPrimitiveProfile(voidValue: VoidValue): PrimitiveInfoProfile =
    infoProducer.newPrimitiveInfoProfile(scalaVirtualMachine, voidValue)

  protected def newObjectProfile(objectReference: ObjectReference): ObjectInfoProfile =
    infoProducer.newObjectInfoProfile(scalaVirtualMachine, objectReference)()

  protected def newStringProfile(stringReference: StringReference): StringInfoProfile =
  infoProducer.newStringInfoProfile(scalaVirtualMachine, stringReference)()

  protected def newArrayProfile(arrayReference: ArrayReference): ArrayInfoProfile =
    infoProducer.newArrayInfoProfile(scalaVirtualMachine, arrayReference)()

  protected def newTypeProfile(_type: Type): TypeInfoProfile =
    infoProducer.newTypeInfoProfile(scalaVirtualMachine, _type)
}
