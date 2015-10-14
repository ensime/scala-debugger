package org.senkbeil.debugger.api.jdi.events.filters.processors

import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.Event
import org.senkbeil.debugger.api.jdi.events.JDIEventArgument
import org.senkbeil.debugger.api.jdi.events.filters.{JDIEventFilterProcessor, MaxTriggerFilter}

/**
 * Represents a processor for the max trigger filter.
 *
 * @param maxTriggerFilter The max trigger filter to use when processing
 */
class MaxTriggerFilterProcessor(
  val maxTriggerFilter: MaxTriggerFilter
) extends JDIEventFilterProcessor {
  private val maxCount = maxTriggerFilter.count
  private val internalCount = new AtomicInteger(0)

  /**
   * Processes the provided event with the filter logic. Increments an internal
   * counter to compare against the desired count.
   *
   * @param event Unused
   *
   * @return True if the event passes through the filter, otherwise false; no
   *         data is included
   */
  override def process(event: Event): Boolean = {
    if (internalCount.get() > maxCount) false
    else internalCount.incrementAndGet() <= maxCount
  }

  /**
   * Resets the internal state of the filter.
   */
  override def reset(): Unit = internalCount.set(0)

  override val argument: JDIEventArgument = maxTriggerFilter
}
