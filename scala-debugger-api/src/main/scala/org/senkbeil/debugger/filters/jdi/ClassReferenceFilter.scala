package org.senkbeil.debugger.filters.jdi

import com.sun.jdi.ReferenceType

/**
 * Represents a filter used to limit requests to a specific class reference.
 * Requests are checked by verifying the class containing the current method
 * being invoked.
 *
 * @note Only used by AccessWatchpointRequest, ClassPrepareRequest,
 *       ExceptionRequest, MethodEntryRequest, MethodExitRequest,
 *       ModificationWatchpointRequest, MonitorContendedEnteredRequest,
 *       MonitorContendedEnterRequest, MonitorWaitedRequest,
 *       MonitorWaitRequest, and StepRequest.
 *
 * @param referenceType The reference type of the class to filter by
 */
case class ClassReferenceFilter(referenceType: ReferenceType)
