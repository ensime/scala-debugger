package org.scaladebugger.api.profiles.frozen.info
import org.scaladebugger.api.profiles.traits.info._

import scala.util.Try

/**
 * Represents type information frozen (snapshot at a point in time) for a remote
 * JVM.
 */
case class FrozenTypeInfo(
  name: String,
  signature: String,
  protected val _arrayType: Try[ArrayTypeInfo],
  protected val _classType: Try[ClassTypeInfo],
  protected val _interfaceType: Try[InterfaceTypeInfo],
  protected val _referenceType: Try[ReferenceTypeInfo],
  protected val _primitiveType: Try[PrimitiveTypeInfo]
) extends FrozenTypeInfoLike
