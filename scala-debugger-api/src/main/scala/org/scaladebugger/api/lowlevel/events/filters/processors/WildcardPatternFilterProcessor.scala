package org.scaladebugger.api.lowlevel.events.filters.processors

//import acyclic.file

import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.events.filters.{JDIEventFilter, JDIEventFilterProcessor, WildcardPatternFilter}

/**
 * Represents a processor for the custom property filter.
 *
 * @param wildcardPatternFilter The custom property filter to use when processing
 */
class WildcardPatternFilterProcessor(
  val wildcardPatternFilter: WildcardPatternFilter
) extends JDIEventFilterProcessor {
  private val pattern = wildcardPatternFilter.pattern
  private val regex = s"\\Q$pattern\\E".replace("*", "\\E.*\\Q")

  /**
   * Processes the provided event with the filter logic.
   *
   * @param event The event to process
   *
   * @return True if the event passes through the filter, otherwise false
   */
  override def process(event: Event): Boolean = event match {
    case classPrepareEvent: ClassPrepareEvent =>
      classPrepareEvent.referenceType().name().matches(regex)
    case classUnloadEvent: ClassUnloadEvent =>
      classUnloadEvent.className().matches(regex)
    case exceptionEvent: ExceptionEvent =>
      exceptionEvent.exception().referenceType().name().matches(regex)
    case methodEntryEvent: MethodEntryEvent =>
      methodEntryEvent.method().name().matches(regex)
    case methodExitEvent: MethodExitEvent =>
      methodExitEvent.method().name().matches(regex)
    case threadDeathEvent: ThreadDeathEvent =>
      threadDeathEvent.thread().name().matches(regex)
    case threadStartEvent: ThreadStartEvent =>
      threadStartEvent.thread().name().matches(regex)
    case _ =>
      true
  }

  /**
   * Resets the internal state of the filter.
   */
  override def reset(): Unit = {}

  override val argument: JDIEventFilter = wildcardPatternFilter
}
