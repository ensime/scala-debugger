package org.senkbeil.debugger.jdi.requests.filters

import com.sun.jdi.ThreadReference
import org.senkbeil.debugger.jdi.requests.JDIRequestProcessor
import org.senkbeil.debugger.jdi.requests.filters.processors.ThreadFilterProcessor

/**
 * Represents a filter used to limit requests to a specific thread.
 *
 * @note Only used by AccessWatchpointRequest, BreakpointRequest,
 *       ExceptionRequest, MethodEntryRequest, MethodExitRequest,
 *       ModificationWatchpointRequest, MonitorContendedEnteredRequest,
 *       MonitorContendedEnterRequest, MonitorWaitedRequest,
 *       MonitorWaitRequest, ThreadDeathEvent, and ThreadStartEvent.
 *
 * @param threadReference The thread reference used to specify the thread
 */
case class ThreadFilter(
  threadReference: ThreadReference
) extends JDIRequestFilter {
  /**
   * Creates a new JDI request processor based on this filter.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestProcessor =
    new ThreadFilterProcessor(this)
}
