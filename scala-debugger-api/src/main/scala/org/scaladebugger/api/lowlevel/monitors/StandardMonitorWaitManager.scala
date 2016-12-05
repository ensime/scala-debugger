package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.{MonitorWaitRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for monitor wait requests.
 *
 * @param eventRequestManager The manager used to create monitor wait requests
 */
class StandardMonitorWaitManager(
  private val eventRequestManager: EventRequestManager
) extends MonitorWaitManager with Logging {
  private val monitorWaitRequests =
    new MultiMap[MonitorWaitRequestInfo, MonitorWaitRequest]

  /**
   * Retrieves the list of monitor wait requests contained by
   * this manager.
   *
   * @return The collection of monitor wait requests in the form of
   *         ids
   */
  override def monitorWaitRequestList: Seq[String] =
    monitorWaitRequests.ids

  /**
   * Creates a new monitor wait request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMonitorWaitRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMonitorWaitRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created monitor wait request with id '$requestId'")
      monitorWaitRequests.putWithId(
        requestId,
        MonitorWaitRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a monitor wait request with the specified id.
   *
   * @param id The id of the Monitor Wait Request
   *
   * @return True if a monitor wait request with the id exists,
   *         otherwise false
   */
  override def hasMonitorWaitRequest(id: String): Boolean = {
    monitorWaitRequests.hasWithId(id)
  }

  /**
   * Retrieves the monitor wait request using the specified id.
   *
   * @param id The id of the Monitor Wait Request
   *
   * @return Some monitor wait request if it exists, otherwise None
   */
  override def getMonitorWaitRequest(
    id: String
  ): Option[MonitorWaitRequest] = {
    monitorWaitRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a monitor wait request with the
   * specified id.
   *
   * @param id The id of the Monitor Wait Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorWaitRequestInfo(id: String): Option[MonitorWaitRequestInfo] = {
    monitorWaitRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified monitor wait request.
   *
   * @param id The id of the Monitor Wait Request
   *
   * @return True if the monitor wait request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorWaitRequest(id: String): Boolean = {
    val request = monitorWaitRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}

