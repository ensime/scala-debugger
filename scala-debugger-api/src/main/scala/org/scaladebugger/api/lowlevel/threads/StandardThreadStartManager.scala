package org.scaladebugger.api.lowlevel.threads

import com.sun.jdi.request.{EventRequestManager, ThreadStartRequest}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{Logging, MultiMap}

import scala.util.Try

/**
 * Represents the manager for thread start requests.
 *
 * @param eventRequestManager The manager used to create thread start requests
 */
class StandardThreadStartManager(
  private val eventRequestManager: EventRequestManager
) extends ThreadStartManager with Logging {
  private val threadStartRequests =
    new MultiMap[ThreadStartRequestInfo, ThreadStartRequest]

  /**
   * Retrieves the list of thread start requests contained by this manager.
   *
   * @return The collection of thread start requests in the form of ids
   */
  override def threadStartRequestList: Seq[String] = threadStartRequests.ids

  /**
   * Creates a new thread start request for the specified class and method.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createThreadStartRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createThreadStartRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created thread start request with id '$requestId'")
      threadStartRequests.putWithId(
        requestId,
        ThreadStartRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a thread start request with the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if a thread start request with the id exists, otherwise false
   */
  override def hasThreadStartRequest(id: String): Boolean = {
    threadStartRequests.hasWithId(id)
  }

  /**
   * Retrieves the thread start request using the specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some thread start request if it exists, otherwise None
   */
  override def getThreadStartRequest(id: String): Option[ThreadStartRequest] = {
    threadStartRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a thread start request with the
   * specified id.
   *
   * @param id The id of the Thread Start Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getThreadStartRequestInfo(id: String): Option[ThreadStartRequestInfo] = {
    threadStartRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified thread start request.
   *
   * @param id The id of the Thread Start Request
   *
   * @return True if the thread start request was removed (if it existed),
   *         otherwise false
   */
  override def removeThreadStartRequest(id: String): Boolean = {
    val request = threadStartRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
