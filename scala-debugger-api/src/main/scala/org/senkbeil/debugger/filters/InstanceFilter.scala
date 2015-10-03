package org.senkbeil.debugger.filters

import com.sun.jdi.ObjectReference

/**
 * Represents a filter used to limit requests to a specific instance of a class.
 *
 * @note Only used by AccessWatchpointRequest, BreakpointRequest,
 *       ExceptionRequest, MethodEntryRequest, MethodExitRequest,
 *       ModificationWatchpointRequest, MonitorContendedEnteredRequest,
 *       MonitorContendedEnterRequest, MonitorWaitedRequest, MonitorWaitRequest,
 *       and StepRequest.
 *
 * @param objectReference The object reference used to specify the instance
 */
case class InstanceFilter(objectReference: ObjectReference)
