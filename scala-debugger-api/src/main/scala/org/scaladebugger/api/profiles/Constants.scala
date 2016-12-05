package org.scaladebugger.api.profiles

/**
 * Contains constants related to the profiles.
 */
object Constants {
  /**
   * For use with a pipeline's close function, indicates that a request should
   * be removed immediately (instead of based on cache) and all associated
   * event handlers should also be removed.
   */
  final val CloseRemoveAll = 1
}

