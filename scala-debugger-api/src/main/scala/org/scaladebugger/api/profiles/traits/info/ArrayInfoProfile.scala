package org.scaladebugger.api.profiles.traits.info
//import acyclic.file

import com.sun.jdi.ArrayReference

import scala.util.Try

/**
 * Represents the interface for array-based interaction.
 */
trait ArrayInfoProfile extends ObjectInfoProfile with CommonInfoProfile {
  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ArrayReference

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
  def tryGetValue(index: Int): Try[ValueInfoProfile] = Try(getValue(index))

  /**
   * Retrieves the value in the array at the specified index.
   *
   * @param index The location in the array to retrieve a value
   * @return The retrieved value
   */
  def getValue(index: Int): ValueInfoProfile

  /**
   * Retrieves the value in the array at the specified index.
   *
   * @param index The location in the array to retrieve a value
   * @return The retrieved value
   */
  def apply(index: Int): ValueInfoProfile = getValue(index)

  /**
   * Retrieves the values in the array starting from the specified index and
   * continuing through the specified length of elements.
   *
   * @param index The location in the array to begin retrieving values
   * @param length The number of values to retrieve, or -1 to retrieve all
   *               remaining values to the end of the array
   * @return Success containing the retrieved values, otherwise a failure
   */
  def tryGetValues(index: Int, length: Int): Try[Seq[ValueInfoProfile]] =
    Try(getValues(index, length))

  /**
   * Retrieves the values in the array starting from the specified index and
   * continuing through the specified length of elements.
   *
   * @param index The location in the array to begin retrieving values
   * @param length The number of values to retrieve, or -1 to retrieve
   *               all remaining values to the end of the array
   * @return The retrieved values
   */
  def getValues(index: Int, length: Int): Seq[ValueInfoProfile]

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
    getValues(index, length)

  /**
   * Retrieves all values from the array.
   *
   * @return Success containing the retrieved values, otherwise a failure
   */
  def tryGetValues: Try[Seq[ValueInfoProfile]] = Try(getValues)

  /**
   * Retrieves all values from the array.
   *
   * @return The retrieved values
   */
  def getValues: Seq[ValueInfoProfile]

  /**
   * Retrieves all values from the array.
   *
   * @return The retrieved values
   */
  def apply(): Seq[ValueInfoProfile] = getValues

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return Success containing the updated value, otherwise a failure
   */
  def trySetValue(index: Int, value: Any): Try[Any] =
    Try(setValue(index, value))

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated value
   */
  def setValue(index: Int, value: Any): Any

  /**
   * Sets the value of the array element at the specified location.
   *
   * @param index The location in the array whose value to overwrite
   * @param value The new value to place in the array
   * @return The updated value
   */
  def update(index: Int, value: Any): Any = setValue(index, value)

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
  def trySetValues(
    index: Int,
    values: Seq[Any],
    srcIndex: Int,
    length: Int
  ): Try[Seq[Any]] = Try(setValues(index, values, srcIndex, length))

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
   * @return The updated values
   */
  def setValues(
    index: Int,
    values: Seq[Any],
    srcIndex: Int,
    length: Int
  ): Seq[Any]

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return Success containing the updated values, otherwise a failure
   */
  def trySetValues(values: Seq[Any]): Try[Seq[Any]] =
    Try(setValues(values))

  /**
   * Sets the values of the array elements to the provided values.
   *
   * @param values The new values to use when overwriting elements in the array
   * @return The updated values
   */
  def setValues(values: Seq[Any]): Seq[Any]
}