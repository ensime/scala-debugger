package org.senkbeil.debugger.test.misc

/**
 * Used by the attaching debugger to continue running to avoid unexpected
 * race conditions when the process finishes before being attached to by
 * the debugger.
 */
object AttachingMain extends App {
  while (true) { Thread.sleep(1000) }
}
