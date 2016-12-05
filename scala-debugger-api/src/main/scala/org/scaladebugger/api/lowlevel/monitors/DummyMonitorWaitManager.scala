package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorWaitRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a monitor wait manager whose operations do nothing.
 */
class DummyMonitorWaitManager extends MonitorWaitManager {
  /**
   * Determines if a monitor wait request with the specified id.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return True if a monitor wait request with the id exists,
   *         otherwise false
   */
  override def hasMonitorWaitRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the monitor wait request using the specified id.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return Some monitor wait request if it exists, otherwise None
   */
  override def getMonitorWaitRequest(
    requestId: String
  ): Option[MonitorWaitRequest] = None

  /**
   * Retrieves the information for a monitor wait request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getMonitorWaitRequestInfo(
    requestId: String
  ): Option[MonitorWaitRequestInfo] = None

  /**
   * Retrieves the list of monitor wait requests contained by
   * this manager.
   *
   * @return The collection of monitor wait requests in the form of
   *         ids
   */
  override def monitorWaitRequestList: Seq[String] = Nil

  /**
   * Removes the specified monitor wait request.
   *
   * @param requestId The id of the Monitor Wait Request
   *
   * @return True if the monitor wait request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorWaitRequest(
    requestId: String
  ): Boolean = false

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
  ): Try[String] = Failure(new DummyOperationException)
}
