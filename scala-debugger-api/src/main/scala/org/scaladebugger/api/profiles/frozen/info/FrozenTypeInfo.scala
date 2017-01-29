package org.scaladebugger.api.profiles.frozen.info
import org.scaladebugger.api.profiles.frozen.FrozenException
import org.scaladebugger.api.profiles.traits.info._

/**
 * Represents type information frozen (snapshot at a point in time) for a remote
 * JVM.
 */
case class FrozenTypeInfo(
  name: String,
  signature: String,
  protected val arrayType: Either[FrozenException, ArrayTypeInfo],
  protected val classType: Either[FrozenException, ClassTypeInfo],
  protected val interfaceType: Either[FrozenException, InterfaceTypeInfo],
  protected val referenceType: Either[FrozenException, ReferenceTypeInfo],
  protected val primitiveType: Either[FrozenException, PrimitiveTypeInfo]
) extends FrozenTypeInfoLike
