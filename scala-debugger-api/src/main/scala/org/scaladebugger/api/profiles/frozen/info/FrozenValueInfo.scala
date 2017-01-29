package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.frozen.FrozenException
import org.scaladebugger.api.profiles.traits.info._

/**
 * Represents value information frozen (snapshot at a point in time) for a
 * remote JVM.
 */
case class FrozenValueInfo(
  `type`: TypeInfo,
  isNull: Boolean,
  isVoid: Boolean,
  protected val localValue: Either[FrozenException, Any],
  protected val arrayInfo: Either[FrozenException, ArrayInfo],
  protected val stringInfo: Either[FrozenException, StringInfo],
  protected val objectInfo: Either[FrozenException, ObjectInfo],
  protected val threadInfo: Either[FrozenException, ThreadInfo],
  protected val threadGroupInfo: Either[FrozenException, ThreadGroupInfo],
  protected val classObjectInfo: Either[FrozenException, ClassObjectInfo],
  protected val classLoaderInfo: Either[FrozenException, ClassLoaderInfo],
  protected val primitiveInfo: Either[FrozenException, PrimitiveInfo]
) extends FrozenValueInfoLike
