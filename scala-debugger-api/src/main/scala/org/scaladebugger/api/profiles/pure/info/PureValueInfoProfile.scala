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
  override def toJavaInfo: ValueInfoProfile = {
    infoProducer.toJavaInfo.newValueInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      value = _value
    )
  }

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
   * Returns the value as a class loader (profile).
   *
   * @return The class loader profile wrapping this value
   */
  @throws[AssertionError]
  override def toClassLoaderInfo: ClassLoaderInfoProfile = {
    assert(isClassLoader, "Value must be a class loader!")
    newClassLoaderProfile(_value.asInstanceOf[ClassLoaderReference])
  }

  /**
   * Returns the value as a class object (profile).
   *
   * @return The class object profile wrapping this value
   */
  @throws[AssertionError]
  override def toClassObjectInfo: ClassObjectInfoProfile = {
    assert(isClassObject, "Value must be a class object!")
    newClassObjectProfile(_value.asInstanceOf[ClassObjectReference])
  }

  /**
   * Returns the value as a thread (profile).
   *
   * @return The thread profile wrapping this value
   */
  @throws[AssertionError]
  override def toThreadInfo: ThreadInfoProfile = {
    assert(isThread, "Value must be a thread!")
    newThreadProfile(_value.asInstanceOf[ThreadReference])
  }

  /**
   * Returns the value as a thread group (profile).
   *
   * @return The thread group profile wrapping this value
   */
  @throws[AssertionError]
  override def toThreadGroupInfo: ThreadGroupInfoProfile = {
    assert(isThreadGroup, "Value must be a thread group!")
    newThreadGroupProfile(_value.asInstanceOf[ThreadGroupReference])
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
   * Returns whether or not this value represents a class loader.
   *
   * @return True if a class loader, otherwise false
   */
  override def isClassLoader: Boolean =
    !isNull && _value.isInstanceOf[ClassLoaderReference]

  /**
   * Returns whether or not this value represents a class object.
   *
   * @return True if a class object, otherwise false
   */
  override def isClassObject: Boolean =
    !isNull && _value.isInstanceOf[ClassObjectReference]

  /**
   * Returns whether or not this value represents a thread.
   *
   * @return True if a thread, otherwise false
   */
  override def isThread: Boolean =
    !isNull && _value.isInstanceOf[ThreadReference]

  /**
   * Returns whether or not this value represents a thread group.
   *
   * @return True if a thread group, otherwise false
   */
  override def isThreadGroup: Boolean =
    !isNull && _value.isInstanceOf[ThreadGroupReference]

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

  protected def newClassLoaderProfile(classLoaderReference: ClassLoaderReference): ClassLoaderInfoProfile =
    infoProducer.newClassLoaderInfoProfile(scalaVirtualMachine, classLoaderReference)()

  protected def newClassObjectProfile(classObjectReference: ClassObjectReference): ClassObjectInfoProfile =
    infoProducer.newClassObjectInfoProfile(scalaVirtualMachine, classObjectReference)()

  protected def newThreadGroupProfile(threadGroupReference: ThreadGroupReference): ThreadGroupInfoProfile =
    infoProducer.newThreadGroupInfoProfile(scalaVirtualMachine, threadGroupReference)()

  protected def newThreadProfile(threadReference: ThreadReference): ThreadInfoProfile =
    infoProducer.newThreadInfoProfile(scalaVirtualMachine, threadReference)()

  protected def newTypeProfile(_type: Type): TypeInfoProfile =
    infoProducer.newTypeInfoProfile(scalaVirtualMachine, _type)
}
