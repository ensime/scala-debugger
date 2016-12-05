package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import com.sun.jdi.request.{EventRequestManager, StepRequest}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.filters.CountFilter
import org.scaladebugger.api.lowlevel.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.utils.{Logging, MultiMap}

import scala.util.Try

/**
 * Represents the manager for step requests.
 *
 * @param eventRequestManager The manager used to create step requests
 */
class StandardStepManager(
  private val eventRequestManager: EventRequestManager
) extends StepManager with Logging {
  private val stepRequests = new MultiMap[StepRequestInfo, StepRequest]

  /**
   * Retrieves the list of steps contained by this manager.
   *
   * @return The collection of steps in the form of thread reference
   */
  override def stepRequestList: Seq[StepRequestInfo] = stepRequests.keys

  /**
   * Retrieves the list of steps contained by this manager.
   *
   * @return The collection of steps by id
   */
  override def stepRequestListById: Seq[String] = stepRequests.ids

  /**
   * Creates and enables a step request for the given thread using the provided
   * size (next valid location or next location on a new line) and depth (into,
   * over, or out of the current frame).
   *
   * @note Includes a default count filter of 1. This can be overridden
   *       by providing a CountFilter(count = ???) as an extra argument.
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
  override def createStepRequestWithId(
    requestId: String,
    removeExistingRequests: Boolean,
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    // Remove existing step requests for thread
    if (removeExistingRequests) removeStepRequest(threadReference)

    val arguments = Seq(
      CountFilter(count = 1),
      SuspendPolicyProperty.EventThread,
      EnabledProperty(value = true)
    ) ++ extraArguments

    val request = Try(eventRequestManager.createStepRequest(
      threadReference, size, depth, arguments: _*
    ))

    if (request.isSuccess) {
      val st = (
        if (depth == StepRequest.STEP_OUT) "out of"
        else if (depth == StepRequest.STEP_OVER) "over"
        else "into"
      ) + " " + (if (size == StepRequest.STEP_LINE) "line" else "min")
      logger.trace(s"Created step $st request with id '$requestId'")
      stepRequests.putWithId(
        requestId,
        StepRequestInfo(
          requestId,
          isPending = false,
          removeExistingRequests,
          threadReference,
          size,
          depth,
          extraArguments
        ),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines whether or not there is a step request for the specified thread.
   *
   * @param threadReference The thread with which the step request is associated
   *
   * @return True if a step request for the thread exists, otherwise false
   */
  override def hasStepRequest(threadReference: ThreadReference): Boolean = {
    stepRequests.hasWithKeyPredicate(_.threadReference == threadReference)
  }

  /**
   * Determines whether or not the step request with the specified id exists.
   *
   * @param requestId The id of the request
   *
   * @return True if a step request with the id exists, otherwise false
   */
  override def hasStepRequestWithId(requestId: String): Boolean = {
    stepRequests.hasWithId(requestId)
  }

  /**
   * Returns the collection of step requests for the specified thread.
   *
   * @param threadReference The thread with which the step is associated
   *
   * @return Some collection of steps for the specified thread if it exists,
   *         otherwise None
   */
  override def getStepRequest(
    threadReference: ThreadReference
  ): Option[Seq[StepRequest]] = {
    val requests = stepRequests.getWithKeyPredicate(
      _.threadReference == threadReference
    )

    if (requests.nonEmpty) Some(requests) else None
  }

  /**
   * Returns the collection of steps with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some step request if the id exists, otherwise None
   */
  override def getStepRequestWithId(
    requestId: String
  ): Option[StepRequest] = {
    stepRequests.getWithId(requestId)
  }

  /**
   * Returns the arguments for a step request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some step arguments if found, otherwise None
   */
  override def getStepRequestInfoWithId(
    requestId: String
  ): Option[StepRequestInfo] = {
    stepRequests.getKeyWithId(requestId)
  }

  /**
   * Removes the step requests for the specified thread.
   *
   * @param threadReference The thread with which the step is associated
   *
   * @return True if successfully removed the step request, otherwise false
   */
  override def removeStepRequest(threadReference: ThreadReference): Boolean = {
    val ids = stepRequests.getIdsWithKeyPredicate(
      _.threadReference == threadReference
    )

    ids.nonEmpty && ids.forall(removeStepRequestWithId)
  }

  /**
   * Removes the step request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if successfully removed the step request, otherwise false
   */
  override def removeStepRequestWithId(
    requestId: String
  ): Boolean = {
    val request = stepRequests.removeWithId(requestId)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
