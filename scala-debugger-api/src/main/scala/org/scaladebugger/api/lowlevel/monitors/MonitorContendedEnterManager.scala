package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorContendedEnterRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for monitor contended enter requests.
 */
trait MonitorContendedEnterManager {
  /**
   * Retrieves the list of monitor contended enter requests contained by
   * this manager.
   *
   * @return The collection of monitor contended enter requests in the form of
   *         ids
   */
  def monitorContendedEnterRequestList: Seq[String]

  /**
   * Creates a new monitor contended enter request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnterRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new monitor contended enter request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnterRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createMonitorContendedEnterRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new monitor contended enter request based on the specified
   * information.
   *
   * @param monitorContendedEnterRequestInfo The information used to create
   *                                           the monitor contended enter
   *                                           request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorContendedEnterRequestFromInfo(
    monitorContendedEnterRequestInfo: MonitorContendedEnterRequestInfo
  ): Try[String] = createMonitorContendedEnterRequestWithId(
    monitorContendedEnterRequestInfo.requestId,
    monitorContendedEnterRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a monitor contended enter request with the specified id.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return True if a monitor contended enter request with the id exists,
   *         otherwise false
   */
  def hasMonitorContendedEnterRequest(requestId: String): Boolean

  /**
   * Retrieves the monitor contended enter request using the specified id.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return Some monitor contended enter request if it exists, otherwise None
   */
  def getMonitorContendedEnterRequest(
    requestId: String
  ): Option[MonitorContendedEnterRequest]

  /**
   * Retrieves the information for a monitor contended enter request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorContendedEnterRequestInfo(
    requestId: String
  ): Option[MonitorContendedEnterRequestInfo]

  /**
   * Removes the specified monitor contended enter request.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return True if the monitor contended enter request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorContendedEnterRequest(
    requestId: String
  ): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
