package org.senkbeil.debugger.jdi.events

import com.sun.jdi.event.Event

/**
  * Represents a processor for a JDI Event.
  */
trait JDIEventProcessor {
  /**
   * Processes the provided event.
   *
   * @param event The event to process
   *
   * @return True if the process was successful (the event passed),
   *         otherwise false
   */
  def process(event: Event): Boolean

  /**
   * Resets the internal state of the processor.
   */
  def reset(): Unit

  /**
   * Represents the argument contained by this processor.
   *
   * @return The specific argument instance
   */
  val argument: JDIEventArgument
}
