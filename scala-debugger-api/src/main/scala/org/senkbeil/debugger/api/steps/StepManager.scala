package org.senkbeil.debugger.api.steps

import com.sun.jdi.{ThreadReference, VirtualMachine}
import com.sun.jdi.request.{EventRequest, StepRequest}
import org.senkbeil.debugger.api.jdi.requests.JDIRequestArgument
import org.senkbeil.debugger.api.jdi.requests.filters.CountFilter
import org.senkbeil.debugger.api.jdi.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.senkbeil.debugger.api.requests.Implicits._

/**
 * Represents the manager for step requests.
 *
 * @param _virtualMachine The virtual machine whose step requests to
 *                        manage
 */
class StepManager(protected val _virtualMachine: VirtualMachine) {
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  /**
   * Creates a new step request.
   *
   * @param threadReference The thread with which to perform the step
   * @param size The size of the step request (LINE/MIN)
   * @param depth The depth of the step request (INTO/OVER/OUT)
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return The newly-created step request
   */
  private def newStepRequest(
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    extraArguments: JDIRequestArgument*
  ): StepRequest = {
    // Remove any existing step requests
    eventRequestManager.deleteEventRequests(eventRequestManager.stepRequests())

    val stepRequest = eventRequestManager.createStepRequest(
      threadReference, size, depth, extraArguments: _*
    )

    stepRequest
  }

  /**
   * Creates a new step request to step into the next called method (in other
   * words, enter the next frame created by a function on the current line or
   * continue forward).
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return The newly-created step request
   */
  def stepInto(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): StepRequest = newStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_INTO,
    Seq(
      EnabledProperty(value = true),
      CountFilter(count = 1),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments: _*
  )

  /**
   * Creates a new step request to step only into caller frames (in other
   * words, exit the current frame of execution).
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return The newly-created step request
   */
  def stepOut(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): StepRequest = newStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_OUT,
    Seq(
      EnabledProperty(value = true),
      CountFilter(count = 1),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments: _*
  )

  /**
   * Creates a new step request to step only into caller frames or the current
   * frame (in other words, ignore any frames created by executing lines).
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return The newly-created step request
   */
  def stepOver(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): StepRequest = newStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_OVER,
    Seq(
      EnabledProperty(value = true),
      CountFilter(count = 1),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments: _*
  )
}
