package org.senkbeil.debugger.requests

import com.sun.jdi._
import com.sun.jdi.request._
import org.senkbeil.debugger.jdi.requests.properties.EnabledProperty
import org.senkbeil.debugger.jdi.requests.{JDIRequestArgumentProcessor, JDIRequestArgument}

/**
 * Represents a wrapper around the event request manager, providing helper
 * methods for added functionality.
 *
 * @param eventRequestManager The underlying manager used to create and delete
 *                            event request instances
 */
class EventRequestManagerWrapper(
  private val eventRequestManager: EventRequestManager
) {

  //
  // CREATION METHODS
  //

  /**
   * Creates a new access watchpoint request.
   *
   * @param field The field to watch for access
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createAccessWatchpointRequest(
    field: Field,
    requestArguments: JDIRequestArgument*
  ): AccessWatchpointRequest = {
    val eventRequest = eventRequestManager.createAccessWatchpointRequest(field)

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new breakpoint request.
   *
   * @param location The location to use for the breakpoint
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createBreakpointRequest(
    location: Location,
    requestArguments: JDIRequestArgument*
  ): BreakpointRequest = {
    val eventRequest = eventRequestManager.createBreakpointRequest(location)

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new class prepare request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createClassPrepareRequest(
    requestArguments: JDIRequestArgument*
  ): ClassPrepareRequest = {
    val eventRequest = eventRequestManager.createClassPrepareRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new class unload request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createClassUnloadRequest(
    requestArguments: JDIRequestArgument*
  ): ClassUnloadRequest = {
    val eventRequest = eventRequestManager.createClassUnloadRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new exception request.
   *
   * @param referenceType The type of exception (and all subclasses) to receive
   *                      reports from
   * @param notifyCaught Whether or not to receive reports from caught
   *                     exceptions
   * @param notifyUncaught Whether or not to receive reports from uncaught
   *                       exceptions
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createExceptionRequest(
    referenceType: ReferenceType,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    requestArguments: JDIRequestArgument*
  ): ExceptionRequest = {
    val eventRequest = eventRequestManager.createExceptionRequest(
      referenceType,
      notifyCaught,
      notifyUncaught
    )

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new method entry request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createMethodEntryRequest(
    requestArguments: JDIRequestArgument*
  ): MethodEntryRequest = {
    val eventRequest = eventRequestManager.createMethodEntryRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new method exit request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createMethodExitRequest(
    requestArguments: JDIRequestArgument*
  ): MethodExitRequest = {
    val eventRequest = eventRequestManager.createMethodExitRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new modification watchpoint request.
   *
   * @param field The field to watch for modification
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createModificationWatchpointRequest(
    field: Field,
    requestArguments: JDIRequestArgument*
  ): ModificationWatchpointRequest = {
    val eventRequest =
      eventRequestManager.createModificationWatchpointRequest(field)

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new monitor contended entered request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createMonitorContendedEnteredRequest(
    requestArguments: JDIRequestArgument*
  ): MonitorContendedEnteredRequest = {
    val eventRequest =
      eventRequestManager.createMonitorContendedEnteredRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new monitor contended enter request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createMonitorContendedEnterRequest(
    requestArguments: JDIRequestArgument*
  ): MonitorContendedEnterRequest = {
    val eventRequest = eventRequestManager.createMonitorContendedEnterRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new monitor waited request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createMonitorWaitedRequest(
    requestArguments: JDIRequestArgument*
  ): MonitorWaitedRequest = {
    val eventRequest = eventRequestManager.createMonitorWaitedRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new monitor wait request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createMonitorWaitRequest(
    requestArguments: JDIRequestArgument*
    ): MonitorWaitRequest = {
    val eventRequest = eventRequestManager.createMonitorWaitRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new step request.
   *
   * @note A depth of min occurs whenever the code index changes.
   * @note A depth of line occurs whenever the source line changes.
   *
   * @param threadReference The reference to the thread in which to step
   * @param size The step size (INTO/OVER/OUT)
   * @param depth The step depth (MIN/LINE)
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createStepRequest(
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    requestArguments: JDIRequestArgument*
  ): StepRequest = {
    val eventRequest = eventRequestManager.createStepRequest(
      threadReference,
      size,
      depth
    )

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new thread death request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createThreadDeathRequest(
    requestArguments: JDIRequestArgument*
  ): ThreadDeathRequest = {
    val eventRequest = eventRequestManager.createThreadDeathRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new thread start request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createThreadStartRequest(
    requestArguments: JDIRequestArgument*
  ): ThreadStartRequest = {
    val eventRequest = eventRequestManager.createThreadStartRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  /**
   * Creates a new virtual machine death request.
   *
   * @param requestArguments The extra arguments to apply to the request
   *
   * @return The updated request instance
   */
  def createVMDeathRequest(
    requestArguments: JDIRequestArgument*
  ): VMDeathRequest = {
    val eventRequest = eventRequestManager.createVMDeathRequest()

    applyArgumentsToEventRequest(eventRequest, requestArguments)
  }

  //
  // HELPER METHODS
  //

  /**
   * Applies all provided arguments to the given event request.
   *
   * @param eventRequest The event request with which to apply arguments
   * @param requestArguments The arguments to apply to the event request
   *
   * @return The updated event request
   */
  protected def applyArgumentsToEventRequest[T <: EventRequest](
    eventRequest: T,
    requestArguments: Seq[JDIRequestArgument]
  ): T = {
    // Separate out all enabled properties, use the last one, and append it to
    // the end of the arguments so other arguments like count filter can still
    // be applied (not allowed after enabled set to true)
    val (normalArguments, enabledProperties) =
      groupEnabledProperties(requestArguments)
    val updatedArguments = normalArguments ++ enabledProperties.lastOption

    val jdiRequestArgumentProcessor =
      new JDIRequestArgumentProcessor(updatedArguments: _*)

    jdiRequestArgumentProcessor.process(eventRequest).asInstanceOf[T]
  }

  /**
   * Groups arguments into normal request arguments and enabled properties.
   *
   * @param requestArguments The arguments to group
   *
   * @return The tuple of normal arguments and enabled properties
   */
  protected def groupEnabledProperties(
    requestArguments: Seq[JDIRequestArgument]
  ): (Seq[JDIRequestArgument], Seq[EnabledProperty]) = {
    val splitArguments =
      requestArguments.groupBy(_.isInstanceOf[EnabledProperty])
    val normalArguments = splitArguments.getOrElse(false, Nil)
    val enabledProperties = splitArguments
      .getOrElse(true, Nil)
      .map(_.asInstanceOf[EnabledProperty])

    (normalArguments, enabledProperties)
  }
}
