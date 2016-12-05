package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.{MonitorContendedEnterRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for monitor contended enter requests.
 *
 * @param eventRequestManager The manager used to create monitor contended
 *                            enter requests
 */
class StandardMonitorContendedEnterManager(
  private val eventRequestManager: EventRequestManager
) extends MonitorContendedEnterManager with Logging {
  private val monitorContendedEnterRequests =
    new MultiMap[MonitorContendedEnterRequestInfo, MonitorContendedEnterRequest]

  /**
   * Retrieves the list of monitor contended enter requests contained by
   * this manager.
   *
   * @return The collection of monitor contended enter requests in the form of
   *         ids
   */
  override def monitorContendedEnterRequestList: Seq[String] =
    monitorContendedEnterRequests.ids

  /**
   * Creates a new monitor contended enter request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMonitorContendedEnterRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMonitorContendedEnterRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      val i = requestId
      logger.trace(s"Created monitor contended enter request with id '$i'")
      monitorContendedEnterRequests.putWithId(
        requestId,
        MonitorContendedEnterRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a monitor contended enter request with the specified id.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return True if a monitor contended enter request with the id exists,
   *         otherwise false
   */
  override def hasMonitorContendedEnterRequest(id: String): Boolean = {
    monitorContendedEnterRequests.hasWithId(id)
  }

  /**
   * Retrieves the monitor contended enter request using the specified id.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return Some monitor contended enter request if it exists, otherwise None
   */
  override def getMonitorContendedEnterRequest(
    id: String
  ): Option[MonitorContendedEnterRequest] = {
    monitorContendedEnterRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a monitor contended enter request with the
   * specified id.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorContendedEnterRequestInfo(
    id: String
  ): Option[MonitorContendedEnterRequestInfo] = {
    monitorContendedEnterRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified monitor contended enter request.
   *
   * @param id The id of the Monitor Contended Enter Request
   *
   * @return True if the monitor contended enter request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorContendedEnterRequest(
    id: String
  ): Boolean = {
    val request = monitorContendedEnterRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}

