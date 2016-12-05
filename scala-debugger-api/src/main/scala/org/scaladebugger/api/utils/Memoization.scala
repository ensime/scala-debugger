package org.scaladebugger.api.utils

import scala.collection.mutable.{Map => MutableMap}

/**
 * Represents a generic form of memoization. Modified from Pathikrit Bhowmick's
 * copy of Scala Memoization.
 *
 * @see https://github.com/pathikrit/scalgos/blob/master/src/main/scala/com/github/pathikrit/scalgos/Memo.scala
 *
 * @param memoFunc Represents the function that will be memoized
 * @param cacheInvalidFunc Represents a function that, if it yields true, will
 *                         mark the current cache element represented by K as
 *                         invalid and rerun the memo function
 * @param cache The map used to store the cache of results to be fed back as
 *              a result of memoization
 * @tparam I Represents the input to the memoized function
 * @tparam K Represents the type of key to use for cached results
 * @tparam O Represents the output of the memoized function
 */
class Memoization[I, K, O](
  private val memoFunc: I => O,
  private val cacheInvalidFunc: K => Boolean = (_: K) => false,
  private val cache: MutableMap[K, O] = MutableMap.empty[K, O]
)(
  implicit convertToKey: I => K
) extends (I => O) {
  /**
   * Clears the internal memoization, allowing new results to be calculated
   * from previously-used input.
   */
  def clear(): Unit = cache.clear()

  /**
   * Executes the memoized function if the given input is new, otherwise
   * returns the result from a previous execution.
   *
   * @param input The input to execute upon
   *
   * @return The result of the execution
   */
  override def apply(input: I) = cache.synchronized {
    val key = convertToKey(input)

    if (!cacheInvalidFunc(key)) {
      cache.getOrElseUpdate(key, memoFunc(input))
    } else {
      cache.put(key, memoFunc(input))
      cache(key)
    }
  }
}

/**
 * Provides additional abstractions on top of memoization.
 */
object Memoization {
  /**
   * Type of a simple memoized function e.g. when I = K
   */
  type ==>[I, O] = Memoization[I, I, O]
}
