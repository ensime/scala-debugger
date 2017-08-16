package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.macros.freeze.{CanFreeze, Freezable}

/**
 * Represents information about a thread's status.
 */
//@Freezable
trait ThreadStatusInfo {
  /**
   * Represents the status code for the thread.
   *
   * @return The status code as a number
   */
  @CanFreeze
  def statusCode: Int

  /**
   * Returns a string representing the status of the thread.
   *
   * @return The status as a string
   */
  def statusString: String = {
    if (isMonitor)            "Monitoring"
    else if (isNotStarted)    "Not Started"
    else if (isRunning)       "Running"
    else if (isSleeping)      "Sleeping"
    else if (isUnknown)       "Unknown"
    else if (isWait)          "Waiting"
    else if (isZombie)        "Zombie"
    else if (isAtBreakpoint)  "Suspended at Breakpoint"
    else if (isSuspended)     "Suspended"
    else                      s"Invalid Status Id $statusCode"
  }

  /**
   * Indicates whether or not the status of this thread is known.
   *
   * @return False if the status is known, otherwise true
   */
  @CanFreeze
  def isUnknown: Boolean

  /**
   * Indicates whether or not this thread is a zombie.
   *
   * @return True if a zombie, otherwise false
   */
  @CanFreeze
  def isZombie: Boolean

  /**
   * Indicates whether or not this thread is running.
   *
   * @return True if running, otherwise false
   */
  @CanFreeze
  def isRunning: Boolean

  /**
   * Indicates whether or not this thread is sleeping.
   *
   * @return True if sleeping, otherwise false
   */
  @CanFreeze
  def isSleeping: Boolean

  /**
   * Indicates whether or not this thread is monitoring.
   *
   * @return True if monitoring, otherwise false
   */
  @CanFreeze
  def isMonitor: Boolean

  /**
   * Indicates whether or not this thread is waiting.
   *
   * @return True if waiting, otherwise false
   */
  @CanFreeze
  def isWait: Boolean

  /**
   * Indicates whether or not this thread has started.
   *
   * @return True if has started, otherwise false
   */
  @CanFreeze
  def isNotStarted: Boolean

  /**
   * Indicates whether or not this thread is suspended at a breakpoint.
   *
   * @return True if suspended at a breakpoint, otherwise false
   */
  @CanFreeze
  def isAtBreakpoint: Boolean

  /**
   * Indicates whether or not this thread is suspended.
   *
   * @return True if suspended, otherwise false
   */
  @CanFreeze
  def isSuspended: Boolean

  /**
   * Indicates the total number of times this thread has been suspended.
   *
   * @return The total number of pending suspensions
   */
  @CanFreeze
  def suspendCount: Int
}
