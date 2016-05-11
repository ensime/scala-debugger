package org.scaladebugger.api.profiles.traits.info

/**
 * Represents information about a thread's status.
 */
trait ThreadStatusInfoProfile {
  /**
   * Represents the status code for the thread.
   *
   * @return The status code as a number
   */
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
  def isUnknown: Boolean

  /**
   * Indicates whether or not this thread is a zombie.
   *
   * @return True if a zombie, otherwise false
   */
  def isZombie: Boolean

  /**
   * Indicates whether or not this thread is running.
   *
   * @return True if running, otherwise false
   */
  def isRunning: Boolean

  /**
   * Indicates whether or not this thread is sleeping.
   *
   * @return True if sleeping, otherwise false
   */
  def isSleeping: Boolean

  /**
   * Indicates whether or not this thread is monitoring.
   *
   * @return True if monitoring, otherwise false
   */
  def isMonitor: Boolean

  /**
   * Indicates whether or not this thread is waiting.
   *
   * @return True if waiting, otherwise false
   */
  def isWait: Boolean

  /**
   * Indicates whether or not this thread has started.
   *
   * @return True if has started, otherwise false
   */
  def isNotStarted: Boolean

  /**
   * Indicates whether or not this thread is suspended at a breakpoint.
   *
   * @return True if suspended at a breakpoint, otherwise false
   */
  def isAtBreakpoint: Boolean

  /**
   * Indicates whether or not this thread is suspended.
   *
   * @return True if suspended, otherwise false
   */
  def isSuspended: Boolean

  /**
   * Indicates the total number of times this thread has been suspended.
   *
   * @return The total number of pending suspensions
   */
  def suspendCount: Int
}
