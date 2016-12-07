package org.scaladebugger.api.dsl.info

import org.scaladebugger.api.profiles.traits.info.{ObjectInfo, ValueInfo}
import org.scaladebugger.api.virtualmachines.ObjectCache

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param valueInfo The profile to wrap
 */
class ValueInfoDSLWrapper[T <: ValueInfo] private[dsl] (
  private val valueInfo: T
) {
  /**
   * Caches this value in its associated JVM cache.
   *
   * @param objectCache The JVM cache to store this value
   * @return The stored value
   */
  def cache()(
    implicit objectCache: ObjectCache = valueInfo.scalaVirtualMachine.cache
  ): T = {
    import org.scaladebugger.api.dsl.Implicits.ObjectInfoDSL
    valueInfo match {
      case obj if obj.isObject => obj.toObjectInfo.cache()
      case _                      =>
    }
    valueInfo
  }

  /**
   * Removes this value from its associated JVM cache.
   *
   * @param objectCache The JVM cache to remove this value
   * @return The removed value
   */
  def uncache()(
    implicit objectCache: ObjectCache = valueInfo.scalaVirtualMachine.cache
  ): T = {
    import org.scaladebugger.api.dsl.Implicits.ObjectInfoDSL
    valueInfo match {
      case obj if obj.isObject => obj.toObjectInfo.uncache()
      case _                      =>
    }
    valueInfo
  }
}
