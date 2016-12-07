package org.scaladebugger.api.virtualmachines

import java.util.concurrent.ConcurrentHashMap

import org.scaladebugger.api.profiles.traits.info.ObjectInfo

import scala.collection.JavaConverters._
import scala.util.Try

import ObjectCache._

object ObjectCache {
  type CacheId = Long
  type CacheValue = ObjectInfo
}

/**
 * Represents a standard cache for objects originating from a virtual machine.
 *
 * @param internalCache Contains the collection of objects by their unique ids
 */
class ObjectCache(
  private val internalCache: collection.mutable.Map[CacheId, CacheValue] =
    new ConcurrentHashMap[CacheId, CacheValue]().asScala
) {
  /**
   * Saves an object into the cache.
   *
   * @param cacheValue The object to save
   * @return Some object id if the object was saved, otherwise None
   */
  def save(cacheValue: CacheValue): Option[CacheId] = {
    val cacheId = Try(cacheValue.uniqueId)
    cacheId.foreach(internalCache.put(_, cacheValue))
    cacheId.toOption
  }

  /**
   * Loads an object from the cache.
   *
   * @param cacheId The id of the object to load
   * @return Some object if the object was cached and is still valid,
   *         otherwise None
   */
  def load(cacheId: CacheId): Option[CacheValue] = {
    val cacheValue = internalCache.get(cacheId)
    cacheValue.map(isValid).flatMap {
      case true   => cacheValue
      case false  => remove(cacheId); None
    }
  }

  /**
   * Returns whether or not the cache has an object with the specified id.
   *
   * @param cacheId The id of the object
   * @return True if the cache has the object, otherwise false
   */
  def has(cacheId: CacheId): Boolean = internalCache.contains(cacheId)

  /**
   * Returns whether or not the cache has the specified object.
   *
   * @param cacheValue The object whose id to use
   * @return True if the cache has the object, otherwise false
   */
  def has(cacheValue: CacheValue): Boolean =
    Try(has(cacheValue.uniqueId)).getOrElse(false)

  /**
   * Removes an object from the cache.
   *
   * @param cacheId The id of the object to remove
   * @return Some object if the object existed and was removed, otherwise None
   */
  def remove(cacheId: CacheId): Option[CacheValue] = {
    internalCache.remove(cacheId)
  }

  /**
   * Removes an object from the cache.
   *
   * @param cacheValue The object to remove
   * @return Some object if the object existed and was removed, otherwise None
   */
  def remove(cacheValue: CacheValue): Option[CacheValue] = {
    Try(cacheValue.uniqueId).toOption.flatMap(internalCache.remove)
  }

  /**
   * Removes all objects in the cache.
   */
  def clear(): Unit = {
    internalCache.clear()
  }

  /**
   * Indicates whether or not the cached object is still valid on the remote
   * JVM.
   *
   * @param cacheValue The cached object to check
   * @return True if valid (on remote JVM), otherwise false
   */
  private def isValid(cacheValue: CacheValue): Boolean = {
    // Check if garbage collected (failures to check should also throw out
    // the cached object)
    val exists = !Try(cacheValue.toJdiInstance.isCollected).getOrElse(true)
    exists
  }
}
