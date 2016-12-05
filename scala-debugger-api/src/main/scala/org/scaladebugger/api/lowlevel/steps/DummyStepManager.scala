package org.scaladebugger.api.lowlevel.steps

import com.sun.jdi.ThreadReference
import com.sun.jdi.request.StepRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a step manager whose operations do nothing.
 */
class DummyStepManager extends StepManager {
  /**
   * Returns the arguments for a step request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some step arguments if found, otherwise None
   */
  override def getStepRequestInfoWithId(
    requestId: String
  ): Option[StepRequestInfo] = None

  /**
   * Determines whether or not the step request with the specified id exists.
   *
   * @param requestId The id of the request
   *
   * @return True if a step request with the id exists, otherwise false
   */
  override def hasStepRequestWithId(requestId: String): Boolean = false

  /**
   * Determines whether or not there is a step request for the specified thread.
   *
   * @param threadReference The thread with which the step request is associated
   *
   * @return True if a step request for the thread exists, otherwise false
   */
  override def hasStepRequest(threadReference: ThreadReference): Boolean = false

  /**
   * Retrieves the list of steps contained by this manager.
   *
   * @return The collection of steps by id
   */
  override def stepRequestListById: Seq[String] = Nil

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
  ): Option[Seq[StepRequest]] = None

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
  override def createStepRequestWithId(
    requestId: String,
    removeExistingRequests: Boolean,
    threadReference: ThreadReference,
    size: Int,
    depth: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)

  /**
   * Returns the collection of steps with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some step request if the id exists, otherwise None
   */
  override def getStepRequestWithId(
    requestId: String
  ): Option[StepRequest] = None

  /**
   * Removes the step requests for the specified thread.
   *
   * @param threadReference The thread with which the step is associated
   *
   * @return True if successfully removed the step request, otherwise false
   */
  override def removeStepRequest(
    threadReference: ThreadReference
  ): Boolean = false

  /**
   * Retrieves the list of steps contained by this manager.
   *
   * @return The collection of steps in the form of thread reference
   */
  override def stepRequestList: Seq[StepRequestInfo] = Nil

  /**
   * Removes the step request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if successfully removed the step request, otherwise false
   */
  override def removeStepRequestWithId(requestId: String): Boolean = false
}
