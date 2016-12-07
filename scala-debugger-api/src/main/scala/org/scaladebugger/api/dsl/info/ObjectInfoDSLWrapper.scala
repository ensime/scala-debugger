package org.scaladebugger.api.dsl.info

import org.scaladebugger.api.profiles.traits.info.ObjectInfo
import org.scaladebugger.api.virtualmachines.ObjectCache

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param objectInfo The profile to wrap
 */
class ObjectInfoDSLWrapper[T <: ObjectInfo] private[dsl] (
  private val objectInfo: T
) {
  /**
   * Caches this object in its associated JVM cache.
   *
   * @param objectCache The JVM cache to store this object
   * @return The stored object
   */
  def cache()(
    implicit objectCache: ObjectCache = objectInfo.scalaVirtualMachine.cache
  ): T = {
    objectCache.save(objectInfo)
    objectInfo
  }

  /**
   * Removes this object from its associated JVM cache.
   *
   * @param objectCache The JVM cache to remove this object
   * @return The removed object
   */
  def uncache()(
    implicit objectCache: ObjectCache = objectInfo.scalaVirtualMachine.cache
  ): T = {
    objectCache.remove(objectInfo.uniqueId)
    objectInfo
  }
}
