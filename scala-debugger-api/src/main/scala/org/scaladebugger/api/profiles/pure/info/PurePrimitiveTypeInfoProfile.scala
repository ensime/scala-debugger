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
) with PrimitiveTypeInfoProfile
