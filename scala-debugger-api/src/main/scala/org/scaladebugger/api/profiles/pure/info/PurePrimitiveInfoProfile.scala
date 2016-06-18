package org.scaladebugger.api.profiles.pure.info

//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine


/**
 * Represents a pure implementation of a value profile that adds no custom
 * logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            primitive
 * @param infoProducer The producer of info-based profile instances
 * @param eitherValue Represents the primitive or void value wrapped by this
 *                   profile
 */
class PurePrimitiveInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val eitherValue: Either[PrimitiveValue, VoidValue]
) extends PureValueInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _value = eitherValue.merge
) with PrimitiveInfoProfile {
  /**
   * Returns the type information for the primitive.
   *
   * @return The profile containing type information
   */
  override def typeInfo: PrimitiveTypeInfoProfile =
    super.typeInfo.toPrimitiveType

  /**
   * Returns the value as a value local to this JVM.
   *
   * @return The value as a local instance
   */
  @throws[AssertionError]
  override def toLocalValue: AnyVal = {
    assert(eitherValue.isLeft, "Cannot retrieve value of void!")

    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    eitherValue.left.get.primitiveValue()
  }

  /**
   * Returns whether or not this primitive is a boolean.
   *
   * @return True if the primitive is a boolean, otherwise false
   */
  override def isBoolean: Boolean =
    eitherValue.left.exists(_.isInstanceOf[BooleanValue])

  /**
   * Returns whether or not this primitive is a float.
   *
   * @return True if the primitive is a float, otherwise false
   */
  override def isFloat: Boolean =
    eitherValue.left.exists(_.isInstanceOf[FloatValue])

  /**
   * Returns whether or not this primitive is a double.
   *
   * @return True if the primitive is a double, otherwise false
   */
  override def isDouble: Boolean =
    eitherValue.left.exists(_.isInstanceOf[DoubleValue])

  /**
   * Returns whether or not this primitive is a integer.
   *
   * @return True if the primitive is a integer, otherwise false
   */
  override def isInteger: Boolean =
    eitherValue.left.exists(_.isInstanceOf[IntegerValue])

  /**
   * Returns whether or not this primitive is a long.
   *
   * @return True if the primitive is a long, otherwise false
   */
  override def isLong: Boolean =
    eitherValue.left.exists(_.isInstanceOf[LongValue])

  /**
   * Returns whether or not this primitive is a char.
   *
   * @return True if the primitive is a char, otherwise false
   */
  override def isChar: Boolean =
    eitherValue.left.exists(_.isInstanceOf[CharValue])

  /**
   * Returns whether or not this primitive is a byte.
   *
   * @return True if the primitive is a byte, otherwise false
   */
  override def isByte: Boolean =
    eitherValue.left.exists(_.isInstanceOf[ByteValue])

  /**
   * Returns whether or not this primitive is a short.
   *
   * @return True if the primitive is a short, otherwise false
   */
  override def isShort: Boolean =
    eitherValue.left.exists(_.isInstanceOf[ShortValue])
}
