package org.scaladebugger.api.lowlevel.events.filters


import org.scaladebugger.api.lowlevel.events.filters.processors.WildcardPatternFilterProcessor

/**
 * Represents a local filter that will result in ignoring any incoming event if
 * it does not match the specified wildcard pattern.
 *
 * @example WildcardPatternFilter(pattern = "java*") will ignore any event not
 *          originating whose method or full class name does not match this
 *          pattern.
 *
 * @note Only valid for ClassPrepare, ClassUnload, Exception, MethodEntry,
 *       MethodExit, ThreadDeath, and ThreadStart events. All other events are
 *       ignored and do not affect the overall filter results.
 *
 * @param pattern The wildcard pattern
 */
case class WildcardPatternFilter(pattern: String) extends JDIEventFilter {
  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor =
    new WildcardPatternFilterProcessor(this)
}
