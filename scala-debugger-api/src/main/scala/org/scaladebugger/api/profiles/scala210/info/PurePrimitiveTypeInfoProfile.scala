package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi.{PrimitiveType, VoidType}
import org.scaladebugger.api.profiles.traits.info.PrimitiveTypeInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a primitive type profile that adds no
 * custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            primitive type
 * @param eitherType Represents the primitive or void type wrapped by this
 *                   profile
 */
class PurePrimitiveTypeInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  private val eitherType: Either[PrimitiveType, VoidType]
) extends PureTypeInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  _type = eitherType.merge
) with PrimitiveTypeInfoProfile
