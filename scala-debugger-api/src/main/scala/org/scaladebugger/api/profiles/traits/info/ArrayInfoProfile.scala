package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.ArrayReference

import scala.util.Try
import ArrayInfoProfile._

/**
 * Contains constants available to all array-focused information profiles.
 */
object ArrayInfoProfile {
  /**
   * Represents the default maximum number of elements to display in a
   * pretty string.
   */
  val DefaultMaxPrettyElements: Int = 3
}

/**
 * Represents the interface for array-based interaction.
 */
trait ArrayInfoProfile
  extends ObjectInfoProfile with CreateInfoProfile with CommonInfoProfile
{
  import scala.reflect.runtime.universe.{TypeTag, typeOf}

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ArrayReference

  /**
   * Returns the type information for the array.
   *
   * @return The profile containing type information
   */
  override def typeInfo: ArrayTypeInfoProfile

  /**
   * Returns the length of the array.
   *
   * @return The length of the array
   */
  def length: Int

  /**
   * Retrieves the value in the array at the specified index.
   *
   * @param index The location in the array to retrieve a value
   * @return Success containing the retrieved value, otherwise a failure
   */
  def tryValue(index: Int): Try[ValueInfoProfile] = Try(value(index))

  /**
   * Retrieves the value in the array at the specified index.
   *
   * @param index The location in the array to retrieve a value
   * @return The retrieved value
   */
  def value(index: Int): ValueInfoProfile

  /**
   * Retrieves the value in the array at the specified index.
   *
   * @param index The location in the array to retrieve a value
   * @return The retrieved value
   */
  def apply(index: Int): ValueInfoProfile = value(index)

  /**
   * Retrieves the values in the array starting from the specified index and
   * continuing through the specified length of elements.
   *
   * @param index The location in the array to begin retrieving values
   * @param length The number of values to retrieve, or -1 to retrieve all
   *               remaining values to the end of the array
   * @return Success containing the retrieved values, otherwise a failure
   */
  def tryValues(index: Int, length: Int): Try[Seq[ValueInfoProfile]] =
    Try(values(index, length))

  /**
   * Retrieves the values in the array starting from the specified index and
   * continuing through the specified length of elements.
   *
   * @param index The location in the array to begin retrieving values
   * @param length The number of values to retrieve, or -1 to retrieve
   *               all remaining values to the end of the array
   * @return The retrieved values
   */
  def values(index: Int, length: Int): Seq[ValueInfoProfile]

  /**
   * Retrieves the values in the array starting from the specified index and
   * continuing through the specified length of elements.
   *
   * @param index The location in the array to begin retrieving values
   * @param length The number of values to retrieve, or -1 to retrieve
   *               all remaining values to the end of the array
   * @return The retrieved values
   */
  def apply(index: Int, length: Int): Seq[ValueInfoProfile] =
    values(index, length)

  /**
   * Retrieves all values from the array.
   *
   * @return Success containing the retrieved values, otherwise a failure
   */
  def tryValues: Try[Seq[ValueInfoProfile]] = Try(values)

  /**
   * Retrieves all values from the array.
   *
   * @return The retrieved values
   */
  def values: Seq[ValueInfoProfile]

  /**
   * Retrieves all values from the array.
   *
   * @return The retrieved values
   */
  def apply(): Seq[ValueInfoProfile] = values

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return Success containing the updated remote value, otherwise a failure
   */
  def trySetValue[T](
    index: Int,
    value: T
  )(implicit typeTag: TypeTag[T]): Try[ValueInfoProfile] =
    Try(setValue(index, value))

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated remote value
   */
  def setValue[T](
    index: Int,
    value: T
  )(implicit typeTag: TypeTag[T]): ValueInfoProfile = {
    setValueFromInfo(index, newValueProfileFromLocalValue(value))
  }

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated remote value
   */
  def update[T](
    index: Int,
    value: T
  )(implicit typeTag: TypeTag[T]): ValueInfoProfile = setValue(index, value)

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return Success containing the updated remote value, otherwise a failure
   */
  def trySetValueFromInfo(
    index: Int,
    value: ValueInfoProfile
  ): Try[ValueInfoProfile] = Try(setValueFromInfo(index, value))

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated remote value
   */
  def setValueFromInfo(index: Int, value: ValueInfoProfile): ValueInfoProfile

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated remote value
   */
  def update(index: Int, value: ValueInfoProfile): ValueInfoProfile =
    setValueFromInfo(index, value)

  /**
   * Sets the values of the array elements starting at the specified location.
   *
   * @param index The location in the array to begin overwriting
   * @param values The new values to use when overwriting elements in the array
   * @param srcIndex The location in the provided value array to begin using
   *                 values to overwrite this array
   * @param length The total number of elements to overwrite, or -1 to
   *               overwrite all elements in the array from the
   *               beginning of the index
   * @return Success containing the updated values, otherwise a failure
   */
  def trySetValues[T](
    index: Int,
    values: Seq[T],
    srcIndex: Int,
    length: Int
  )(implicit typeTag: TypeTag[T]): Try[Seq[ValueInfoProfile]] =
    Try(setValues(index, values, srcIndex, length))

  /**
   * Sets the values of the array elements starting at the specified location.
   *
   * @param index The location in the array to begin overwriting
   * @param values The new values to use when overwriting elements in the array
   * @param srcIndex The location in the provided value array to begin using
   *                 values to overwrite this array
   * @param length The total number of elements to overwrite, or -1 to
   *               overwrite all elements in the array from the
   *               beginning of the index
   * @return The updated remote values
   */
  def setValues[T](
    index: Int,
    values: Seq[T],
    srcIndex: Int,
    length: Int
  )(implicit typeTag: TypeTag[T]): Seq[ValueInfoProfile] = setValuesFromInfo(
    index,
    values.map(newValueProfileFromLocalValue(_: T)(typeTag)),
    srcIndex,
    length
  )

  /**
   * Sets the values of the array elements starting at the specified location.
   *
   * @param index The location in the array to begin overwriting
   * @param values The new values to use when overwriting elements in the array
   * @param srcIndex The location in the provided value array to begin using
   *                 values to overwrite this array
   * @param length The total number of elements to overwrite, or -1 to
   *               overwrite all elements in the array from the
   *               beginning of the index
   * @return Success containing the updated remote values, otherwise a failure
   */
  def trySetValuesFromInfo(
    index: Int,
    values: Seq[ValueInfoProfile],
    srcIndex: Int,
    length: Int
  ): Try[Seq[ValueInfoProfile]] =
    Try(setValuesFromInfo(index, values, srcIndex, length))

  /**
   * Sets the values of the array elements starting at the specified location.
   *
   * @param index The location in the array to begin overwriting
   * @param values The new values to use when overwriting elements in the array
   * @param srcIndex The location in the provided value array to begin using
   *                 values to overwrite this array
   * @param length The total number of elements to overwrite, or -1 to
   *               overwrite all elements in the array from the
   *               beginning of the index
   * @return The updated remote values
   */
  def setValuesFromInfo(
    index: Int,
    values: Seq[ValueInfoProfile],
    srcIndex: Int,
    length: Int
  ): Seq[ValueInfoProfile]

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return Success containing the updated remote values, otherwise a failure
   */
  def trySetValues[T](
    values: Seq[T]
  )(implicit typeTag: TypeTag[T]): Try[Seq[ValueInfoProfile]] =
    Try(setValues(values))

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return The updated remote values
   */
  def setValues[T](
    values: Seq[T]
  )(implicit typeTag: TypeTag[T]): Seq[ValueInfoProfile] = {
    setValuesFromInfo(values.map(newValueProfileFromLocalValue(_: T)(typeTag)))
  }

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return Success containing the updated remote values, otherwise a failure
   */
  def trySetValuesFromInfo(
    values: Seq[ValueInfoProfile]
  ): Try[Seq[ValueInfoProfile]] = Try(setValuesFromInfo(values))

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return The updated remote values
   */
  def setValuesFromInfo(values: Seq[ValueInfoProfile]): Seq[ValueInfoProfile]

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @return The human-readable description
   */
  override def toPrettyString: String = toPrettyString(DefaultMaxPrettyElements)

  /**
   * Returns a string presenting a better human-readable description of
   * the JDI instance.
   *
   * @param maxElements The maximum number of elements to retrieve from the
   *                    array (starting from the beginning) to display in
   *                    the string
   * @return The human-readable description
   */
  def toPrettyString(maxElements: Int): String = {
    val l = this.length
    val prefix = s"Array(length = $l)"

    // Retrieve N arguments if possible, returning less elements (or none)
    // depending on the size of the array
    val args = this.tryValues(0, maxElements)
      .map(_.map(_.toPrettyString).mkString(","))
      .getOrElse("<ERROR>")

    if (l == 0)                 s"$prefix[<EMPTY>]"
    else if (l <= maxElements)  s"$prefix[$args]"
    else                        s"$prefix[$args,...]"
  }

  protected def newValueProfileFromLocalValue[T](
    value: T
  )(implicit typeTag: TypeTag[T]): ValueInfoProfile = {
    if (typeTag.tpe <:< typeOf[AnyVal]) {
      createRemotely(value.asInstanceOf[AnyVal])
    } else value match {
      case s: String  => createRemotely(s)
      case v          => throw new UnsupportedTypeException(v)
    }
  }
}
