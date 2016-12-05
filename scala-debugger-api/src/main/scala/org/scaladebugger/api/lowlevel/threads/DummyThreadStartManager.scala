package org.scaladebugger.api.lowlevel.threads

import com.sun.jdi.request.ThreadStartRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a thread start manager whose operations do nothing.
 */
class DummyThreadStartManager extends ThreadStartManager {
  /**
   * Determines if a thread start request with the specified id.
   *
   * @param requestId The id of the Thread Start Request
   *
   * @return True if a thread start request with the id exists,
   *         otherwise false
   */
  override def hasThreadStartRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the thread start request using the specified id.
   *
   * @param requestId The id of the Thread Start Request
   *
   * @return Some thread start request if it exists, otherwise None
   */
  override def getThreadStartRequest(
    requestId: String
  ): Option[ThreadStartRequest] = None

  /**
   * Retrieves the information for a thread start request with the
   * specified id.
   *
   * @param requestId The id of the Thread Start Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getThreadStartRequestInfo(
    requestId: String
  ): Option[ThreadStartRequestInfo] = None

  /**
   * Retrieves the list of thread start requests contained by
   * this manager.
   *
   * @return The collection of thread start requests in the form of
   *         ids
   */
  override def threadStartRequestList: Seq[String] = Nil

  /**
   * Removes the specified thread start request.
   *
   * @param requestId The id of the Thread Start Request
   *
   * @return True if the thread start request was removed
   *         (if it existed), otherwise false
   */
  override def removeThreadStartRequest(
    requestId: String
  ): Boolean = false

  /**
   * Creates a new thread start request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createThreadStartRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)
}
