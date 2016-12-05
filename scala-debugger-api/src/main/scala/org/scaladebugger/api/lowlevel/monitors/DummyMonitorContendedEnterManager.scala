package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorContendedEnterRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a monitor contended enter manager whose operations do nothing.
 */
class DummyMonitorContendedEnterManager extends MonitorContendedEnterManager {
  /**
   * Determines if a monitor contended enter request with the specified id.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return True if a monitor contended enter request with the id exists,
   *         otherwise false
   */
  override def hasMonitorContendedEnterRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the monitor contended enter request using the specified id.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return Some monitor contended enter request if it exists, otherwise None
   */
  override def getMonitorContendedEnterRequest(
    requestId: String
  ): Option[MonitorContendedEnterRequest] = None

  /**
   * Retrieves the information for a monitor contended enter request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getMonitorContendedEnterRequestInfo(
    requestId: String
  ): Option[MonitorContendedEnterRequestInfo] = None

  /**
   * Retrieves the list of monitor contended enter requests contained by
   * this manager.
   *
   * @return The collection of monitor contended enter requests in the form of
   *         ids
   */
  override def monitorContendedEnterRequestList: Seq[String] = Nil

  /**
   * Removes the specified monitor contended enter request.
   *
   * @param requestId The id of the Monitor Contended Enter Request
   *
   * @return True if the monitor contended enter request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorContendedEnterRequest(
    requestId: String
  ): Boolean = false

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
  ): Try[String] = Failure(new DummyOperationException)
}
