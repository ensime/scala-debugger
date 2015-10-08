package org.senkbeil.debugger.jdi.events.processors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.Event
import org.senkbeil.debugger.jdi.events.{JDIEventProcessor, JDIEventArgument}
import org.senkbeil.debugger.jdi.events.filters.MinTriggerFilter

/**
 * Represents a processor for the min trigger filter.
 *
 * @param minTriggerFilter The min trigger filter to use when processing
 */
class MinTriggerProcessor(
  val minTriggerFilter: MinTriggerFilter
) extends JDIEventProcessor {
  private val minCount = minTriggerFilter.count
  private val internalCount = new AtomicInteger(0)

  /**
   * Processes the provided event with the filter logic. Increments an internal
   * counter to compare against the desired count.
   *
   * @param event Unused
   *
   * @return True if the event passes through the filter, otherwise false
   */
  override def process(event: Event): Boolean = {
    if (internalCount.get() <= minCount)
      internalCount.incrementAndGet() > minCount
    else
      true
  }

  /**
   * Resets the internal state of the filter.
   */
  override def reset(): Unit = internalCount.set(0)

  override val argument: JDIEventArgument = minTriggerFilter
}
