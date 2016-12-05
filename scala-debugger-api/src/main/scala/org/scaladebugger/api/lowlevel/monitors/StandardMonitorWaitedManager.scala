package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.{MonitorWaitedRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for monitor waited requests.
 *
 * @param eventRequestManager The manager used to create monitor waited requests
 */
class StandardMonitorWaitedManager(
  private val eventRequestManager: EventRequestManager
) extends MonitorWaitedManager with Logging {
  private val monitorWaitedRequests =
    new MultiMap[MonitorWaitedRequestInfo, MonitorWaitedRequest]

  /**
   * Retrieves the list of monitor waited requests contained by
   * this manager.
   *
   * @return The collection of monitor waited requests in the form of
   *         ids
   */
  override def monitorWaitedRequestList: Seq[String] =
    monitorWaitedRequests.ids

  /**
   * Creates a new monitor waited request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMonitorWaitedRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMonitorWaitedRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created monitor waited request with id '$requestId'")
      monitorWaitedRequests.putWithId(
        requestId,
        MonitorWaitedRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a monitor waited request with the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return True if a monitor waited request with the id exists,
   *         otherwise false
   */
  override def hasMonitorWaitedRequest(id: String): Boolean = {
    monitorWaitedRequests.hasWithId(id)
  }

  /**
   * Retrieves the monitor waited request using the specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return Some monitor waited request if it exists, otherwise None
   */
  override def getMonitorWaitedRequest(
    id: String
  ): Option[MonitorWaitedRequest] = {
    monitorWaitedRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a monitor waited request with the
   * specified id.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorWaitedRequestInfo(
    id: String
  ): Option[MonitorWaitedRequestInfo] = {
    monitorWaitedRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified monitor waited request.
   *
   * @param id The id of the Monitor Waited Request
   *
   * @return True if the monitor waited request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorWaitedRequest(id: String): Boolean = {
    val request = monitorWaitedRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}

