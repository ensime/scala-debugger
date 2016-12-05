package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorWaitedRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for monitor waited requests.
 */
trait MonitorWaitedManager {
  /**
   * Retrieves the list of monitor waited requests contained by
   * this manager.
   *
   * @return The collection of monitor waited requests in the form of
   *         ids
   */
  def monitorWaitedRequestList: Seq[String]

  /**
   * Creates a new monitor waited request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitedRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new monitor waited request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitedRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createMonitorWaitedRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new monitor waited request based on the specified information.
   *
   * @param monitorWaitedRequestInfo The information used to create the
   *                                 monitor waited request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitedRequestFromInfo(
    monitorWaitedRequestInfo: MonitorWaitedRequestInfo
  ): Try[String] = createMonitorWaitedRequestWithId(
    monitorWaitedRequestInfo.requestId,
    monitorWaitedRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a monitor waited request with the specified id.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return True if a monitor waited request with the id exists,
   *         otherwise false
   */
  def hasMonitorWaitedRequest(requestId: String): Boolean

  /**
   * Retrieves the monitor waited request using the specified id.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return Some monitor waited request if it exists, otherwise None
   */
  def getMonitorWaitedRequest(requestId: String): Option[MonitorWaitedRequest]

  /**
   * Retrieves the information for a monitor waited request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorWaitedRequestInfo(
    requestId: String
  ): Option[MonitorWaitedRequestInfo]

  /**
   * Removes the specified monitor waited request.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return True if the monitor waited request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorWaitedRequest(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
