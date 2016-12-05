package org.scaladebugger.api.lowlevel.wrappers

import com.sun.jdi._

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a wrapper around a value, providing additional methods.
 *
 * @param _value The value to wrap
 */
class ValueWrapper(private val _value: Value) {
  require(_value != null, "Value cannot be null!")

  /** Represents the Scala identifier for the module container */
  private val ModuleFieldName = """MODULE$"""

  /**
   * Indicates whether or not the wrapped value is an object reference.
   *
   * @return True if the wrapped value is an object, otherwise false
   */
  val isObject: Boolean = _value.isInstanceOf[ObjectReference]

  /**
   * Indicates whether or not the wrapped value is a primitive value.
   *
   * @return True if the wrapped value is a primitive, otherwise false
   */
  val isPrimitive: Boolean = _value.isInstanceOf[PrimitiveValue]

  /**
   * Retrieves the actual value representing this value.
   *
   * @throws Throwable If value is neither a primitive nor an object
   *
   * @return The value instance if available
   */
  @throws[Throwable]
  def value(): Any = {
    if (isPrimitive) primitiveValue()
    else if (isObject) objectValue()
    else throw new Throwable(s"Unknown value '${_value.toString}'!")
  }

  /**
   * Retrieves the actual value representing this value.
   *
   * @return Some(value) if available, otherwise None
   */
  def valueAsOption(): Option[Any] = Try(this.value()).toOption

  /**
   * Retrieves the actual primitive value represented by this value.
   *
   * @throws IllegalArgumentException If the value is not a primitive value
   *
   * @return The primitive value represented by this instance
   */
  @throws[IllegalArgumentException]
  def primitiveValue(): AnyVal = {
    require(isPrimitive, "Value is not a primitive!")

    _value match {
      case booleanValue: BooleanValue     => booleanValue.value()
      case byteValue: ByteValue           => byteValue.value()
      case charValue: CharValue           => charValue.value()
      case doubleValue: DoubleValue       => doubleValue.value()
      case floatValue: FloatValue         => floatValue.value()
      case integerValue: IntegerValue     => integerValue.value()
      case longValue: LongValue           => longValue.value()
      case shortValue: ShortValue         => shortValue.value()
      case primitiveValue: PrimitiveValue =>
        throw new Throwable("Unknown primitive: " + primitiveValue)
    }
  }

  /**
   * Retrieves the actual primitive value as an option.
   *
   * @return Some primitive value if available, otherwise None
   */
  def primitiveValueAsOption(): Option[AnyVal] =
    Try(this.primitiveValue()).toOption

  /**
   * Retrieves a representation of the object.
   *
   * @throws IllegalArgumentException If the value is not an object reference
   *
   * @return The value representing the object reference (varies by type)
   */
  @throws[IllegalArgumentException]
  def objectValue(): AnyRef = {
    require(isObject, "Value is not an object!")

    _value match {
      case stringReference: StringReference => stringReference.value()
      case arrayReference: ArrayReference   => arrayReference.getValues
      case _                                => _value.toString
    }
  }

  /**
   * Retrieves a representation of the object.
   *
   * @return Some value if available, otherwise None
   */
  def objectValueAsOption(): Option[AnyRef] = Try(this.objectValue()).toOption

  /**
   * Retrieves the immediate visible fields and associated values for this
   * specific value.
   *
   * @throws IllegalArgumentException If the value is not an object reference
   *
   * @return The map of field -> value pairings
   */
  @throws[IllegalArgumentException]
  def fieldsAndValues(): Map[Field, Value] = {
    require(isObject, "Value is not an object!")

    // TODO: Handle more specific cases like StringReference and
    //       ClassloaderReference
    _value match {
      case objectReference: ObjectReference =>
        // TODO: Use objectReference.values(
        // objectReference.referenceType().visibleFields())
        // TODO: Filter out reference that equals this object instead of
        //       checking exclusively for MODULE$
        objectReference.referenceType().visibleFields().asScala
          .filterNot(_.name() == ModuleFieldName).map { field =>
            field -> Try(objectReference.getValue(field)).getOrElse(null)
          }.toMap
      case obj =>
        throw new Throwable("Unknown object: " + obj)
    }
  }

  /**
   * Retrieves the immediate visible fields and associated values for this
   * specific value.
   *
   * @return Some map of fields and values if available, otherwise None
   */
  def fieldsAndValuesAsOption(): Option[Map[Field, Value]] =
    Try(this.fieldsAndValues()).toOption

  /**
   * Constructs a string representing this value (with no recursion).
   *
   * @return The string representing this value
   */
  override def toString: String = toString(1)

  /**
   * Constructs a string representing this value and recursively this value's
   * fields (if it has any) up to the maximum level.
   *
   * @param maxRecursionLevel The maximum level of recursion for building
   *                          this value's string
   *
   * @return The string representing this value
   */
  def toString(maxRecursionLevel: Int): String = {
    val returnStringBuilder = new StringBuilder

    buildString(
      stringBuilder = returnStringBuilder,
      maxRecursionLevel = maxRecursionLevel,
      currentRecursionLevel = 0
    )

    returnStringBuilder.toString().trim
  }

  /**
   * Builds a string representing this value.
   *
   * @param stringBuilder The string builder to use
   * @param maxRecursionLevel The maximum level of recursion
   * @param currentRecursionLevel The current level of recursion
   */
  private def buildString(
    stringBuilder: StringBuilder,
    maxRecursionLevel: Int,
    currentRecursionLevel: Int
  ): Unit = {
    // Exit if reached maximum level of recursion
    if (currentRecursionLevel >= maxRecursionLevel) return

    // Append self to string
    stringBuilder.append(_value.toString + "\n")

    // If reference, for each field, try to append its own string
    val newRecursionLevel = currentRecursionLevel + 1
    val valueWrapper = new ValueWrapper(_value)
    if (valueWrapper.isObject && newRecursionLevel < maxRecursionLevel) {
      valueWrapper.fieldsAndValues().foreach {
        case (field, value) =>
          stringBuilder.append("\t" * newRecursionLevel + field.name() + ": ")

          new ValueWrapper(value).buildString(
            stringBuilder, maxRecursionLevel, newRecursionLevel
          )
      }
    }
  }
}
