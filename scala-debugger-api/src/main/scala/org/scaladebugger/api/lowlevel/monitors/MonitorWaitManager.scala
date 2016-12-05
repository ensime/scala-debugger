package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorWaitRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for monitor wait requests.
 */
trait MonitorWaitManager {
  /**
   * Retrieves the list of monitor wait requests contained by
   * this manager.
   *
   * @return The collection of monitor wait requests in the form of
   *         ids
   */
  def monitorWaitRequestList: Seq[String]

  /**
   * Creates a new monitor wait request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new monitor wait request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createMonitorWaitRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new monitor wait request based on the specified information.
   *
   * @param monitorWaitRequestInfo The information used to create the
   *                               monitor wait request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMonitorWaitRequestFromInfo(
    monitorWaitRequestInfo: MonitorWaitRequestInfo
  ): Try[String] = createMonitorWaitRequestWithId(
    monitorWaitRequestInfo.requestId,
    monitorWaitRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a monitor wait request with the specified id.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return True if a monitor wait request with the id exists,
   *         otherwise false
   */
  def hasMonitorWaitRequest(requestId: String): Boolean

  /**
   * Retrieves the monitor wait request using the specified id.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return Some monitor wait request if it exists, otherwise None
   */
  def getMonitorWaitRequest(requestId: String): Option[MonitorWaitRequest]

  /**
   * Retrieves the information for a monitor wait request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getMonitorWaitRequestInfo(
    requestId: String
  ): Option[MonitorWaitRequestInfo]

  /**
   * Removes the specified monitor wait request.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return True if the monitor wait request was removed
   *         (if it existed), otherwise false
   */
  def removeMonitorWaitRequest(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
