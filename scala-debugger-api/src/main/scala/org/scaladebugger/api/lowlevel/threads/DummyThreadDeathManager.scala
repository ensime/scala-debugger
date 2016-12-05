package org.scaladebugger.api.lowlevel.threads

import com.sun.jdi.request.ThreadDeathRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a thread death manager whose operations do nothing.
 */
class DummyThreadDeathManager extends ThreadDeathManager {
  /**
   * Determines if a thread death request with the specified id.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return True if a thread death request with the id exists,
   *         otherwise false
   */
  override def hasThreadDeathRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the thread death request using the specified id.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return Some thread death request if it exists, otherwise None
   */
  override def getThreadDeathRequest(
    requestId: String
  ): Option[ThreadDeathRequest] = None

  /**
   * Retrieves the information for a thread death request with the
   * specified id.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getThreadDeathRequestInfo(
    requestId: String
  ): Option[ThreadDeathRequestInfo] = None

  /**
   * Retrieves the list of thread death requests contained by
   * this manager.
   *
   * @return The collection of thread death requests in the form of
   *         ids
   */
  override def threadDeathRequestList: Seq[String] = Nil

  /**
   * Removes the specified thread death request.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return True if the thread death request was removed
   *         (if it existed), otherwise false
   */
  override def removeThreadDeathRequest(
    requestId: String
  ): Boolean = false

  /**
   * Creates a new thread death request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createThreadDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)
}
