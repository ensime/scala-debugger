package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure implementation of an array profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param _arrayReference The reference to the underlying JDI array
 * @param _virtualMachine The virtual machine used to mirror local values on
 *                       the remote JVM
 * @param _referenceType The reference type for this array
 */
class PureArrayInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val _arrayReference: ArrayReference
)(
  override protected val _virtualMachine: VirtualMachine = _arrayReference.virtualMachine(),
  private val _referenceType: ReferenceType = _arrayReference.referenceType()
) extends PureObjectInfo(scalaVirtualMachine, infoProducer, _arrayReference)(
  _virtualMachine = _virtualMachine,
  _referenceType = _referenceType
) with ArrayInfo with PureCreateInfoProfile {
  import scala.collection.JavaConverters._
  import org.scaladebugger.api.lowlevel.wrappers.Implicits._
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
  override def toJavaInfo: ArrayInfo = {
    infoProducer.toJavaInfo.newArrayInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      arrayReference = _arrayReference
    )(
      virtualMachine = _virtualMachine,
      referenceType = _referenceType
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ArrayReference = _arrayReference

  /**
   * Returns the type information for the array.
   *
   * @return The profile containing type information
   */
  override def typeInfo: ArrayTypeInfo = super.typeInfo.toArrayType

  /**
   * Returns the length of the array.
   *
   * @return The length of the array
   */
  override def length: Int = _arrayReference.length()

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated remote value
   */
  override def setValueFromInfo(
    index: Int,
    value: ValueInfo
  ): ValueInfo = {
    _arrayReference.setValue(index, value.toJdiInstance)
    value
  }

  /**
   * Sets the values of the array elements starting at the specified location.
   *
   * @param index    The location in the array to begin overwriting
   * @param values   The new values to use when overwriting elements in the array
   * @param srcIndex The location in the provided value array to begin using
   *                 values to overwrite this array
   * @param length   The total number of elements to overwrite, or -1 to
   *                 overwrite all elements in the array from the
   *                 beginning of the index
   * @return The updated remote values
   */
  override def setValuesFromInfo(
    index: Int,
    values: Seq[ValueInfo],
    srcIndex: Int,
    length: Int
  ): Seq[ValueInfo] = {
    import scala.collection.JavaConverters._
    val v = values.map(_.toJdiInstance).asJava
    _arrayReference.setValues(index, v, srcIndex, length)

    val sliceIndex = if (length >= 0) srcIndex + length else values.length
    values.slice(srcIndex, sliceIndex)
  }

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return The updated remote values
   */
  override def setValuesFromInfo(
    values: Seq[ValueInfo]
  ): Seq[ValueInfo] = {
    import scala.collection.JavaConverters._
    _arrayReference.setValues(values.map(_.toJdiInstance).asJava)
    values
  }

  /**
   * Retrieves the values in the array starting from the specified index and
   * continuing through the specified length of elements.
   *
   * @param index  The location in the array to begin retrieving values
   * @param length The number of values to retrieve, or -1 to retrieve all
   *               remaining values to the end of the array
   * @return The retrieved values
   */
  override def values(
    index: Int,
    length: Int
  ): Seq[ValueInfo] = {
    _arrayReference.getValues(index, length).asScala.map(newValueProfile)
  }

  /**
   * Retrieves all values from the array.
   *
   * @return The retrieved values
   */
  override def values: Seq[ValueInfo] = {
    _arrayReference.getValues.asScala.map(newValueProfile)
  }

  /**
   * Retrieves the value in the array at the specified index.
   *
   * @param index The location in the array to retrieve a value
   * @return The retrieved value
   */
  override def value(index: Int): ValueInfo = {
    newValueProfile(_arrayReference.getValue(index))
  }

  override protected def newValueProfile(value: Value): ValueInfo =
    infoProducer.newValueInfo(scalaVirtualMachine, value)
}
