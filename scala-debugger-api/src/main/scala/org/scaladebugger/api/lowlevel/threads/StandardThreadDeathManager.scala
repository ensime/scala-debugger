package org.scaladebugger.api.lowlevel.threads

import com.sun.jdi.request.{EventRequestManager, ThreadDeathRequest}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for thread death requests.
 *
 * @param eventRequestManager The manager used to create thread death requests
 */
class StandardThreadDeathManager(
  private val eventRequestManager: EventRequestManager
) extends ThreadDeathManager with Logging {
  private val threadDeathRequests =
    new MultiMap[ThreadDeathRequestInfo, ThreadDeathRequest]

  /**
   * Retrieves the list of thread death requests contained by this manager.
   *
   * @return The collection of thread death requests in the form of ids
   */
  override def threadDeathRequestList: Seq[String] = threadDeathRequests.ids

  /**
   * Creates a new thread death request for the specified class and method.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createThreadDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createThreadDeathRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created thread death request with id '$requestId'")
      threadDeathRequests.putWithId(
        requestId,
        ThreadDeathRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a thread death request with the specified id.
   *
   * @param id The id of the Thread Death Request
   *
   * @return True if a thread death request with the id exists, otherwise false
   */
  override def hasThreadDeathRequest(id: String): Boolean = {
    threadDeathRequests.hasWithId(id)
  }

  /**
   * Retrieves the thread death request using the specified id.
   *
   * @param id The id of the Thread Death Request
   *
   * @return Some thread death request if it exists, otherwise None
   */
  override def getThreadDeathRequest(id: String): Option[ThreadDeathRequest] = {
    threadDeathRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a thread death request with the
   * specified id.
   *
   * @param id The id of the Thread Death Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getThreadDeathRequestInfo(id: String): Option[ThreadDeathRequestInfo] = {
    threadDeathRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified thread death request.
   *
   * @param id The id of the Thread Death Request
   *
   * @return True if the thread death request was removed (if it existed),
   *         otherwise false
   */
  override def removeThreadDeathRequest(id: String): Boolean = {
    val request = threadDeathRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
