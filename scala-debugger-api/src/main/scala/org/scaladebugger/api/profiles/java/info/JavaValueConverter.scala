package org.scaladebugger.api.profiles.java.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import JavaValueConverter._

object JavaValueConverter {
   val DefaultNewPrimitiveInfo: JavaValueInfo => PrimitiveValue => PrimitiveInfo =
     value => value.infoProducer.newPrimitiveInfo(value.scalaVirtualMachine, _: PrimitiveValue)
   val DefaultNewVoidInfo: JavaValueInfo => VoidValue => PrimitiveInfo =
     value => value.infoProducer.newPrimitiveInfo(value.scalaVirtualMachine, _: VoidValue)
   val DefaultNewObjectInfo: JavaValueInfo => ObjectReference => ObjectInfo =
     value => value.infoProducer.newObjectInfo(value.scalaVirtualMachine, _: ObjectReference)()
   val DefaultNewStringInfo: JavaValueInfo => StringReference => StringInfo =
     value => value.infoProducer.newStringInfo(value.scalaVirtualMachine, _: StringReference)()
   val DefaultNewArrayInfo: JavaValueInfo => ArrayReference => ArrayInfo =
     value => value.infoProducer.newArrayInfo(value.scalaVirtualMachine, _: ArrayReference)()
   val DefaultNewClassLoaderInfo: JavaValueInfo => ClassLoaderReference => ClassLoaderInfo =
     value => value.infoProducer.newClassLoaderInfo(value.scalaVirtualMachine, _: ClassLoaderReference)()
   val DefaultNewClassObjectInfo: JavaValueInfo => ClassObjectReference => ClassObjectInfo =
     value => value.infoProducer.newClassObjectInfo(value.scalaVirtualMachine, _: ClassObjectReference)()
   val DefaultNewThreadGroupInfo: JavaValueInfo => ThreadGroupReference => ThreadGroupInfo =
     value => value.infoProducer.newThreadGroupInfo(value.scalaVirtualMachine, _: ThreadGroupReference)()
   val DefaultNewThreadInfo: JavaValueInfo => ThreadReference => ThreadInfo =
     value => value.infoProducer.newThreadInfo(value.scalaVirtualMachine, _: ThreadReference)()

  /**
   * Creates a new converter from the provided value.
   * @param value The value to convert
   * @return The converter capable of converting the provided value
   */
  def from(value: JavaValueInfo): JavaValueConverter =
    new JavaValueConverter(value)()
}

class JavaValueConverter private(private val value: JavaValueInfo)(
  private val newPrimitiveInfo: PrimitiveValue => PrimitiveInfo =
    DefaultNewPrimitiveInfo(value),
  private val newVoidInfo: VoidValue => PrimitiveInfo =
    DefaultNewVoidInfo(value),
  private val newObjectInfo: ObjectReference => ObjectInfo =
    DefaultNewObjectInfo(value),
  private val newStringInfo: StringReference => StringInfo =
    DefaultNewStringInfo(value),
  private val newArrayInfo: ArrayReference => ArrayInfo =
    DefaultNewArrayInfo(value),
  private val newClassLoaderInfo: ClassLoaderReference => ClassLoaderInfo =
    DefaultNewClassLoaderInfo(value),
  private val newClassObjectInfo: ClassObjectReference => ClassObjectInfo =
    DefaultNewClassObjectInfo(value),
  private val newThreadGroupInfo: ThreadGroupReference => ThreadGroupInfo =
    DefaultNewThreadGroupInfo(value),
  private val newThreadInfo: ThreadReference => ThreadInfo =
    DefaultNewThreadInfo(value)
) extends ValueConverter {
  /**
   * Creates a copy of the converter that will use the specified converter
   * functions over the current instances.
   * @param newPrimitiveInfo
   * @param newVoidInfo
   * @param newObjectInfo
   * @param newStringInfo
   * @param newArrayInfo
   * @param newClassLoaderInfo
   * @param newClassObjectInfo
   * @param newThreadGroupInfo
   * @param newThreadInfo
   * @return The new converter instance
   */
  def using(
     newPrimitiveInfo: PrimitiveValue => PrimitiveInfo = newPrimitiveInfo,
     newVoidInfo: VoidValue => PrimitiveInfo = newVoidInfo,
     newObjectInfo: ObjectReference => ObjectInfo = newObjectInfo,
     newStringInfo: StringReference => StringInfo = newStringInfo,
     newArrayInfo: ArrayReference => ArrayInfo = newArrayInfo,
     newClassLoaderInfo: ClassLoaderReference => ClassLoaderInfo = newClassLoaderInfo,
     newClassObjectInfo: ClassObjectReference => ClassObjectInfo = newClassObjectInfo,
     newThreadGroupInfo: ThreadGroupReference => ThreadGroupInfo = newThreadGroupInfo,
     newThreadInfo: ThreadReference => ThreadInfo = newThreadInfo
  ): JavaValueConverter = new JavaValueConverter(value)(
    newPrimitiveInfo = newPrimitiveInfo,
    newVoidInfo = newVoidInfo,
    newObjectInfo = newObjectInfo,
    newStringInfo = newStringInfo,
    newArrayInfo = newArrayInfo,
    newClassLoaderInfo = newClassLoaderInfo,
    newClassObjectInfo = newClassObjectInfo,
    newThreadGroupInfo = newThreadGroupInfo
  )

  /**
   * Returns the value as an array (profile).
   *
   * @return The array profile wrapping this value
   */
  @throws[AssertionError]
  override def toArrayInfo: ArrayInfo = {
    assert(value.isArray, "Value must be an array!")
    newArrayInfo(value._value.asInstanceOf[ArrayReference])
  }

  /**
   * Returns the value as a class loader (profile).
   *
   * @return The class loader profile wrapping this value
   */
  @throws[AssertionError]
  override def toClassLoaderInfo: ClassLoaderInfo = {
    assert(value.isClassLoader, "Value must be a class loader!")
    newClassLoaderInfo(value._value.asInstanceOf[ClassLoaderReference])
  }

  /**
   * Returns the value as a class object (profile).
   *
   * @return The class object profile wrapping this value
   */
  @throws[AssertionError]
  override def toClassObjectInfo: ClassObjectInfo = {
    assert(value.isClassObject, "Value must be a class object!")
    newClassObjectInfo(value._value.asInstanceOf[ClassObjectReference])
  }

  /**
   * Returns the value as a thread (profile).
   *
   * @return The thread profile wrapping this value
   */
  @throws[AssertionError]
  override def toThreadInfo: ThreadInfo = {
    assert(value.isThread, "Value must be a thread!")
    newThreadInfo(value._value.asInstanceOf[ThreadReference])
  }

  /**
   * Returns the value as a thread group (profile).
   *
   * @return The thread group profile wrapping this value
   */
  @throws[AssertionError]
  override def toThreadGroupInfo: ThreadGroupInfo = {
    assert(value.isThreadGroup, "Value must be a thread group!")
    newThreadGroupInfo(value._value.asInstanceOf[ThreadGroupReference])
  }

  /**
   * Returns the value as an object (profile).
   *
   * @return The object profile wrapping this value
   */
  @throws[AssertionError]
  override def toObjectInfo: ObjectInfo = {
    assert(value.isObject, "Value must be an object!")
    newObjectInfo(value._value.asInstanceOf[ObjectReference])
  }

  /**
   * Returns the value as a string (profile).
   *
   * @return The string profile wrapping this value
   */
  @throws[AssertionError]
  override def toStringInfo: StringInfo = {
    assert(value.isString, "Value must be a string!")
    newStringInfo(value._value.asInstanceOf[StringReference])
  }

  /**
   * Returns the value as a primitive (profile).
   *
   * @return The primitive profile wrapping this value
   */
  @throws[AssertionError]
  override def toPrimitiveInfo: PrimitiveInfo = {
    assert(value.isPrimitive, "Value must be a primitive!")
    value._value match {
      case p: PrimitiveValue => newPrimitiveInfo(p)
      case v: VoidValue      => newVoidInfo(v)
    }
  }
}
