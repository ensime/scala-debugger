package org.scaladebugger.api.lowlevel.threads

import com.sun.jdi.request.ThreadDeathRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for thread death requests.
 */
trait ThreadDeathManager {
  /**
   * Retrieves the list of thread death requests contained by this manager.
   *
   * @return The collection of thread death requests in the form of ids
   */
  def threadDeathRequestList: Seq[String]

  /**
   * Creates a new thread death request for the specified class and method.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new thread death request for the specified class and method.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadDeathRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createThreadDeathRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new thread death request based on the specified information.
   *
   * @param threadDeathRequestInfo The information used to create the
   *                                thread death request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createThreadDeathRequestFromInfo(
    threadDeathRequestInfo: ThreadDeathRequestInfo
  ): Try[String] = createThreadDeathRequestWithId(
    threadDeathRequestInfo.requestId,
    threadDeathRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a thread death request with the specified id.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return True if a thread death request with the id exists, otherwise false
   */
  def hasThreadDeathRequest(requestId: String): Boolean

  /**
   * Retrieves the thread death request using the specified id.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return Some thread death request if it exists, otherwise None
   */
  def getThreadDeathRequest(requestId: String): Option[ThreadDeathRequest]

  /**
   * Retrieves the information for a thread death request with the
   * specified id.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getThreadDeathRequestInfo(
    requestId: String
  ): Option[ThreadDeathRequestInfo]

  /**
   * Removes the specified thread death request.
   *
   * @param requestId The id of the Thread Death Request
   *
   * @return True if the thread death request was removed (if it existed),
   *         otherwise false
   */
  def removeThreadDeathRequest(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
