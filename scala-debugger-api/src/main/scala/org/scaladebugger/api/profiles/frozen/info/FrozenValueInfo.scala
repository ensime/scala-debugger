package org.scaladebugger.api.profiles.frozen.info

import org.scaladebugger.api.profiles.traits.info._

import scala.util.Try

/**
 * Represents value information frozen (snapshot at a point in time) for a
 * remote JVM.
 */
case class FrozenValueInfo(
  `type`: TypeInfo,
  isNull: Boolean,
  isVoid: Boolean,
  protected val _localValue: Try[Any],
  protected val _arrayInfo: Try[ArrayInfo],
  protected val _stringInfo: Try[StringInfo],
  protected val _objectInfo: Try[ObjectInfo],
  protected val _threadInfo: Try[ThreadInfo],
  protected val _threadGroupInfo: Try[ThreadGroupInfo],
  protected val _classObjectInfo: Try[ClassObjectInfo],
  protected val _classLoaderInfo: Try[ClassLoaderInfo],
  protected val _primitiveInfo: Try[PrimitiveInfo]
) extends FrozenValueInfoLike
