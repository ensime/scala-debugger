package org.scaladebugger.api.lowlevel.watchpoints

import com.sun.jdi.request.AccessWatchpointRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a access watchpoint manager whose operations do nothing.
 */
class DummyAccessWatchpointManager extends AccessWatchpointManager {
  /**
   * Determines if a access watchpoint request with the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return True if a access watchpoint request with the id exists,
   *         otherwise false
   */
  override def hasAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = false

  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoint request information
   */
  override def accessWatchpointRequestList: Seq[AccessWatchpointRequestInfo] = Nil

  /**
   * Returns the collection of access watchpoint requests representing the
   * access watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return Some collection of access watchpoints for the field,
   *         or None if the specified field has no access watchpoints
   */
  override def getAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Option[Seq[AccessWatchpointRequest]] = None

  /**
   * Removes the access watchpoint request with the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   * @return True if the access watchpoint request was removed
   *         (if it existed), otherwise false
   */
  override def removeAccessWatchpointRequestWithId(
    id: String
  ): Boolean = false

  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoints by id
   */
  override def accessWatchpointRequestListById: Seq[String] = Nil

  /**
   * Removes the access watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return True if successfully removed access watchpoint,
   *         otherwise false
   */
  override def removeAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = false

  /**
   * Determines if a access watchpoint request with the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   * @return True if a access watchpoint request with the id exists,
   *         otherwise false
   */
  override def hasAccessWatchpointRequestWithId(
    id: String
  ): Boolean = false

  /**
   * Retrieves the access watchpoint request using the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   * @return Some access watchpoint request if it exists, otherwise None
   */
  override def getAccessWatchpointRequestWithId(
    id: String
  ): Option[AccessWatchpointRequest] = None

  /**
   * Returns the information for an access watchpoint request with the
   * specified id.
   *
   * @param requestId The id of the request
   * @return Some access watchpoint information if found, otherwise None
   */
  override def getAccessWatchpointRequestInfoWithId(
    requestId: String
  ): Option[AccessWatchpointRequestInfo] = None

  /**
   * Creates a new access watchpoint request for the specified field
   * using the field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   * @return Success(id) if successful, otherwise Failure
   */
  override def createAccessWatchpointRequestWithId(
    requestId: String,
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)
}
