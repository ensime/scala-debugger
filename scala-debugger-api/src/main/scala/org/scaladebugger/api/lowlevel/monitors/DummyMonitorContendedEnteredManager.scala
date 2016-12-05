package org.scaladebugger.api.lowlevel.monitors

import com.sun.jdi.request.MonitorContendedEnteredRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a monitor contended entered manager whose operations do nothing.
 */
class DummyMonitorContendedEnteredManager extends MonitorContendedEnteredManager {
  /**
   * Determines if a monitor contended entered request with the specified id.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return True if a monitor contended entered request with the id exists,
   *         otherwise false
   */
  override def hasMonitorContendedEnteredRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the monitor contended entered request using the specified id.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return Some monitor contended entered request if it exists, otherwise None
   */
  override def getMonitorContendedEnteredRequest(
    requestId: String
  ): Option[MonitorContendedEnteredRequest] = None

  /**
   * Retrieves the information for a monitor contended entered request with the
   * specified id.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getMonitorContendedEnteredRequestInfo(
    requestId: String
  ): Option[MonitorContendedEnteredRequestInfo] = None

  /**
   * Retrieves the list of monitor contended entered requests contained by
   * this manager.
   *
   * @return The collection of monitor contended entered requests in the form of
   *         ids
   */
  override def monitorContendedEnteredRequestList: Seq[String] = Nil

  /**
   * Removes the specified monitor contended entered request.
   *
   * @param requestId The id of the Monitor Contended Entered Request
   *
   * @return True if the monitor contended entered request was removed
   *         (if it existed), otherwise false
   */
  override def removeMonitorContendedEnteredRequest(
    requestId: String
  ): Boolean = false

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
  ): Try[String] = Failure(new DummyOperationException)
}
