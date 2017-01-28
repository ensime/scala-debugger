package org.scaladebugger.api.profiles.java.info

import com.sun.jdi.{PrimitiveType, Type, VoidType}
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, PrimitiveTypeInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a java implementation of a primitive type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            primitive type
 * @param infoProducer The producer of info-based profile instances
 * @param eitherType Represents the primitive or void type wrapped by this
 *                   profile
 */
class JavaPrimitiveTypeInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val eitherType: Either[PrimitiveType, VoidType]
) extends JavaTypeInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  _type = eitherType.merge
) with PrimitiveTypeInfo {
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
  override def toJavaInfo: PrimitiveTypeInfo = {
    val producer = infoProducer.toJavaInfo
    eitherType match {
      case Left(pt) => producer.newPrimitiveTypeInfo(
        scalaVirtualMachine = scalaVirtualMachine,
        primitiveType = pt
      )
      case Right(vt) => producer.newPrimitiveTypeInfo(
        scalaVirtualMachine = scalaVirtualMachine,
        voidType = vt
      )
    }
  }
}
