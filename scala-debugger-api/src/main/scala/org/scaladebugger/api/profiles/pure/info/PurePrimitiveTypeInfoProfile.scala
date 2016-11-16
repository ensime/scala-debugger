package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi.{PrimitiveType, Type, VoidType}
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, PrimitiveTypeInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a primitive type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            primitive type
 * @param infoProducer The producer of info-based profile instances
 * @param eitherType Represents the primitive or void type wrapped by this
 *                   profile
 */
class PurePrimitiveTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val eitherType: Either[PrimitiveType, VoidType]
) extends PureTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _type = eitherType.merge
) with PrimitiveTypeInfoProfile {
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
  override def toJavaInfo: PrimitiveTypeInfoProfile = {
    val producer = infoProducer.toJavaInfo
    eitherType match {
      case Left(pt) => producer.newPrimitiveTypeInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        primitiveType = pt
      )
      case Right(vt) => producer.newPrimitiveTypeInfoProfile(
        scalaVirtualMachine = scalaVirtualMachine,
        voidType = vt
      )
    }
  }
}
