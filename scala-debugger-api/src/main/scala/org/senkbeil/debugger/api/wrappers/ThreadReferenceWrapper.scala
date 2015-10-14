package org.senkbeil.debugger.api.wrappers

import com.sun.jdi.ThreadReference

/**
 * Represents a wrapper around a thread reference, providing additional methods.
 *
 * @param _threadReference The thread reference to wrap
 */
class ThreadReferenceWrapper(private val _threadReference: ThreadReference) {
  require(_threadReference != null, "Thread reference cannot be null!")

  /**
   * Indicates whether or not the status of this thread is known.
   *
   * @return False if the status is known, otherwise true
   */
  def isUnknown: Boolean =
    _threadReference.status() == ThreadReference.THREAD_STATUS_UNKNOWN

  /**
   * Indicates whether or not this thread is a zombie.
   *
   * @return True if a zombie, otherwise false
   */
  def isZombie: Boolean =
    _threadReference.status() == ThreadReference.THREAD_STATUS_ZOMBIE

  /**
   * Indicates whether or not this thread is running.
   *
   * @return True if running, otherwise false
   */
  def isRunning: Boolean =
    _threadReference.status() == ThreadReference.THREAD_STATUS_RUNNING

  /**
   * Indicates whether or not this thread is sleeping.
   *
   * @return True if sleeping, otherwise false
   */
  def isSleeping: Boolean =
    _threadReference.status() == ThreadReference.THREAD_STATUS_SLEEPING

  /**
   * Indicates whether or not this thread is monitoring.
   *
   * @return True if monitoring, otherwise false
   */
  def isMonitor: Boolean =
    _threadReference.status() == ThreadReference.THREAD_STATUS_MONITOR

  /**
   * Indicates whether or not this thread is waiting.
   *
   * @return True if waiting, otherwise false
   */
  def isWait: Boolean =
    _threadReference.status() == ThreadReference.THREAD_STATUS_WAIT

  /**
   * Indicates whether or not this thread has started.
   *
   * @return True if has started, otherwise false
   */
  def isNotStarted: Boolean =
    _threadReference.status() == ThreadReference.THREAD_STATUS_NOT_STARTED
}
