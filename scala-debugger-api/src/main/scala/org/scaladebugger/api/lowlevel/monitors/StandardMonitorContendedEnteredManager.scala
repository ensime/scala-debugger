package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.{MonitorContendedEnteredRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for monitor contended entered requests.
 *
 * @param eventRequestManager The manager used to create monitor contended
 *                            entered requests
 */
class StandardMonitorContendedEnteredManager(
  private val eventRequestManager: EventRequestManager
) extends MonitorContendedEnteredManager with Logging {
  private val monitorContendedEnteredRequests =
    new MultiMap[MonitorContendedEnteredRequestInfo, MonitorContendedEnteredRequest]

  /**
   * Retrieves the list of monitor contended entered requests contained by
   * this manager.
   *
   * @return The collection of monitor contended entered requests in the form of
   *         ids
   */
  override def monitorContendedEnteredRequestList: Seq[String] =
    monitorContendedEnteredRequests.ids

  /**
   * Creates a new monitor contended entered request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMonitorContendedEnteredRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMonitorContendedEnteredRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      val i = requestId
      logger.trace(s"Created monitor contended entered request with id '$i'")
      monitorContendedEnteredRequests.putWithId(
        requestId,
        MonitorContendedEnteredRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a monitor contended entered request with the specified id.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return True if a monitor contended entered request with the id exists,
   *         otherwise false
   */
  override def hasMonitorContendedEnteredRequest(id: String): Boolean = {
    monitorContendedEnteredRequests.hasWithId(id)
  }

  /**
   * Retrieves the monitor contended entered request using the specified id.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return Some monitor contended entered request if it exists, otherwise None
   */
  override def getMonitorContendedEnteredRequest(
    id: String
  ): Option[MonitorContendedEnteredRequest] = {
    monitorContendedEnteredRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a monitor contended entered request with the
   * specified id.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorContendedEnteredRequestInfo(
    id: String
  ): Option[MonitorContendedEnteredRequestInfo] = {
    monitorContendedEnteredRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified monitor contended entered request.
   *
   * @param id The id of the Monitor Contended Entered Request
   *
   * @return True if the monitor contended entered request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorContendedEnteredRequest(
    id: String
  ): Boolean = {
    val request = monitorContendedEnteredRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
