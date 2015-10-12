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
   * @return The result of processing the event
   */
  def process(event: Event): Any

  /**
   * Represents the argument contained by this processor.
   *
   * @return The specific argument instance
   */
  val argument: JDIEventArgument
}
