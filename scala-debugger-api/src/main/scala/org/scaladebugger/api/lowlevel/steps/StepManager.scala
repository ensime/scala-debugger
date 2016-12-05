package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import com.sun.jdi.request.{EventRequestManager, StepRequest}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.filters.CountFilter
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.MultiMap

import scala.util.Try

/**
 * Represents the manager for step requests.
 */
trait StepManager {
  /**
   * Creates a new step request to step into the next called method (in other
   * words, enter the next frame created by a function on the current line or
   * continue forward) for the next line location.
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepIntoLineRequest(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_INTO,
    extraArguments: _*
  )

  /**
   * Creates a new step request to step only into caller frames (in other
   * words, exit the current frame of execution) for the next line location.
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepOutLineRequest(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_OUT,
    extraArguments: _*
  )

  /**
   * Creates a new step request to step only into caller frames or the current
   * frame (in other words, ignore any frames created by executing lines) for
   * the next line location.
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepOverLineRequest(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequest(
    threadReference,
    StepRequest.STEP_LINE,
    StepRequest.STEP_OVER,
    extraArguments: _*
  )

  /**
   * Creates a new step request to step into the next called method (in other
   * words, enter the next frame created by a function on the current line or
   * continue forward) for the next possible location.
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepIntoMinRequest(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequest(
    threadReference,
    StepRequest.STEP_MIN,
    StepRequest.STEP_INTO,
    extraArguments: _*
  )

  /**
   * Creates a new step request to step only into caller frames (in other
   * words, exit the current frame of execution) for the next possible location.
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepOutMinRequest(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequest(
    threadReference,
    StepRequest.STEP_MIN,
    StepRequest.STEP_OUT,
    extraArguments: _*
  )

  /**
   * Creates a new step request to step only into caller frames or the current
   * frame (in other words, ignore any frames created by executing lines) for
   * the next possible location.
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param threadReference The thread where the step will occur
   * @param extraArguments The additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepOverMinRequest(
    threadReference: ThreadReference,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequest(
    threadReference,
    StepRequest.STEP_MIN,
    StepRequest.STEP_OVER,
    extraArguments: _*
  )

  /**
   * Retrieves the list of steps contained by this manager.
   *
   * @return The collection of steps in the form of thread reference
   */
  def stepRequestList: Seq[StepRequestInfo]

  /**
   * Retrieves the list of steps contained by this manager.
   *
   * @return The collection of steps by id
   */
  def stepRequestListById: Seq[String]

  /**
   * Creates and enables a step request for the given thread using the provided
   * size (next valid location or next location on a new line) and depth (into,
   * over, or out of the current frame).
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param threadReference The thread with which to perform the step
   * @param size The size of the step request (LINE/MIN)
   * @param depth The depth of the step request (INTO/OVER/OUT)
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepRequestWithId(
    requestId: String,
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequestWithId(
    requestId = requestId,
    removeExistingRequests = true,
    threadReference = threadReference,
    size = size,
    depth = depth,
    extraArguments: _*
  )

  /**
   * Creates and enables a step request for the given thread using the provided
   * size (next valid location or next location on a new line) and depth (into,
   * over, or out of the current frame).
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param removeExistingRequests If true, will first remove any existing
   *                               step requests for the specified thread
   * @param threadReference The thread with which to perform the step
   * @param size The size of the step request (LINE/MIN)
   * @param depth The depth of the step request (INTO/OVER/OUT)
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepRequestWithId(
    requestId: String,
    removeExistingRequests: Boolean,
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates and enables a step request for the given thread using the provided
   * size (next valid location or next location on a new line) and depth (into,
   * over, or out of the current frame).
   *
   * Removes any existing step requests for the specified thread.
   *
   * @note Includes a default count filter of 1. This can be overridden by
   *       providing a CountFilter(count = ???) as an extra argument.
   *
   * @param threadReference The thread with which to perform the step
   * @param size The size of the step request (LINE/MIN)
   * @param depth The depth of the step request (INTO/OVER/OUT)
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepRequest(
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createStepRequestWithId(
    newRequestId(),
    threadReference,
    size,
    depth,
    extraArguments: _*
  )

  /**
   * Creates a step request based on the specified information.
   *
   * @param stepRequestInfo The information used to create the step request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createStepRequestFromInfo(
    stepRequestInfo: StepRequestInfo
  ): Try[String] = createStepRequestWithId(
    stepRequestInfo.requestId,
    stepRequestInfo.removeExistingRequests,
    stepRequestInfo.threadReference,
    stepRequestInfo.size,
    stepRequestInfo.depth,
    stepRequestInfo.extraArguments: _*
  )

  /**
   * Determines whether or not there is a step request for the specified thread.
   *
   * @param threadReference The thread with which the step request is associated
   *
   * @return True if a step request for the thread exists, otherwise false
   */
  def hasStepRequest(threadReference: ThreadReference): Boolean

  /**
   * Determines whether or not the step request with the specified id exists.
   *
   * @param requestId The id of the request
   *
   * @return True if a step request with the id exists, otherwise false
   */
  def hasStepRequestWithId(requestId: String): Boolean

  /**
   * Returns the collection of step requests for the specified thread.
   *
   * @param threadReference The thread with which the step is associated
   *
   * @return Some collection of steps for the specified thread if it exists,
   *         otherwise None
   */
  def getStepRequest(threadReference: ThreadReference): Option[Seq[StepRequest]]

  /**
   * Returns the collection of steps with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some step request if the id exists, otherwise None
   */
  def getStepRequestWithId(requestId: String): Option[StepRequest]

  /**
   * Returns the arguments for a step request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some step arguments if found, otherwise None
   */
  def getStepRequestInfoWithId(requestId: String): Option[StepRequestInfo]

  /**
   * Removes the step requests for the specified thread.
   *
   * @param threadReference The thread with which the step is associated
   *
   * @return True if successfully removed the step request, otherwise false
   */
  def removeStepRequest(threadReference: ThreadReference): Boolean

  /**
   * Removes the step request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if successfully removed the step request, otherwise false
   */
  def removeStepRequestWithId(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
