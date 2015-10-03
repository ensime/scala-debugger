package org.senkbeil.debugger.filters.jdi

import com.sun.jdi.ThreadReference

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
case class ThreadFilter(threadReference: ThreadReference)
