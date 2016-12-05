package org.scaladebugger.api.lowlevel

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Represents the common interface inherited by all components that offer
 * pending request support in some fashion.
 */
trait PendingRequestSupport {
  private val pendingSupport = new AtomicBoolean(true)

  /**
   * Enables pending support.
   */
  def enablePendingSupport(): Unit = setPendingSupport(true)

  /**
   * Disables pending support.
   */
  def disablePendingSupport(): Unit = setPendingSupport(false)

  /**
   * Sets enablement of pending support to the specified value.
   *
   * @param value True if enabling support, otherwise false
   */
  def setPendingSupport(value: Boolean): Unit = pendingSupport.set(value)

  /**
   * Indicates whether or not pending support is enabled.
   *
   * @return True if pending support enabled, otherwise false
   */
  def isPendingSupportEnabled: Boolean = pendingSupport.get()
}
