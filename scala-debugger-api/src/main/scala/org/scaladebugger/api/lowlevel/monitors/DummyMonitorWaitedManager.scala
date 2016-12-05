package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorWaitedRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a monitor waited manager whose operations do nothing.
 */
class DummyMonitorWaitedManager extends MonitorWaitedManager {
  /**
   * Determines if a monitor waited request with the specified id.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return True if a monitor waited request with the id exists,
   *         otherwise false
   */
  override def hasMonitorWaitedRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the monitor waited request using the specified id.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return Some monitor waited request if it exists, otherwise None
   */
  override def getMonitorWaitedRequest(
    requestId: String
  ): Option[MonitorWaitedRequest] = None

  /**
   * Retrieves the information for a monitor waited request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getMonitorWaitedRequestInfo(
    requestId: String
  ): Option[MonitorWaitedRequestInfo] = None

  /**
   * Retrieves the list of monitor waited requests contained by
   * this manager.
   *
   * @return The collection of monitor waited requests in the form of
   *         ids
   */
  override def monitorWaitedRequestList: Seq[String] = Nil

  /**
   * Removes the specified monitor waited request.
   *
   * @param requestId The id of the Monitor Waited Request
   *
   * @return True if the monitor waited request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorWaitedRequest(
    requestId: String
  ): Boolean = false

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
  ): Try[String] = Failure(new DummyOperationException)
}
