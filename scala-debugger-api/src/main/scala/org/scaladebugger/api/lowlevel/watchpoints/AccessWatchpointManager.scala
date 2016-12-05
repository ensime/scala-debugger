package org.scaladebugger.api.lowlevel.watchpoints

import com.sun.jdi.request.{AccessWatchpointRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{Logging, MultiMap}

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

/**
 * Represents the manager for access watchpoint requests.
 */
trait AccessWatchpointManager {
  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoint request information
   */
  def accessWatchpointRequestList: Seq[AccessWatchpointRequestInfo]

  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoints by id
   */
  def accessWatchpointRequestListById: Seq[String]

  /**
   * Creates a new access watchpoint request for the specified field using the
   * field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createAccessWatchpointRequestWithId(
    requestId: String,
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new access watchpoint request for the specified field using the
   * field's name.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createAccessWatchpointRequest(
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createAccessWatchpointRequestWithId(
    newRequestId(),
    className,
    fieldName,
    extraArguments: _*
  )

  /**
   * Creates a access watchpoint request based on the specified information.
   *
   * @param accessWatchpointRequestInfo The information used to create the
   *                                    access watchpoint request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createAccessWatchpointRequestFromInfo(
    accessWatchpointRequestInfo: AccessWatchpointRequestInfo
  ): Try[String] = createAccessWatchpointRequestWithId(
    accessWatchpointRequestInfo.requestId,
    accessWatchpointRequestInfo.className,
    accessWatchpointRequestInfo.fieldName,
    accessWatchpointRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a access watchpoint request with the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if a access watchpoint request with the id exists,
   *         otherwise false
   */
  def hasAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean

  /**
   * Determines if a access watchpoint request with the specified id.
   *
   * @param requestId The id of the Access Watchpoint Request
   *
   * @return True if a access watchpoint request with the id exists,
   *         otherwise false
   */
  def hasAccessWatchpointRequestWithId(requestId: String): Boolean

  /**
   * Returns the collection of access watchpoint requests representing the
   * access watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return Some collection of access watchpoints for the field, or None if
   *         the specified field has no access watchpoints
   */
  def getAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Option[Seq[AccessWatchpointRequest]]

  /**
   * Retrieves the access watchpoint request using the specified id.
   *
   * @param requestId The id of the Access Watchpoint Request
   *
   * @return Some access watchpoint request if it exists, otherwise None
   */
  def getAccessWatchpointRequestWithId(
    requestId: String
  ): Option[AccessWatchpointRequest]

  /**
   * Returns the information for an access watchpoint request with the
   * specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some access watchpoint information if found, otherwise None
   */
  def getAccessWatchpointRequestInfoWithId(
    requestId: String
  ): Option[AccessWatchpointRequestInfo]

  /**
   * Removes the access watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if successfully removed access watchpoint, otherwise false
   */
  def removeAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean

  /**
   * Removes the access watchpoint request with the specified id.
   *
   * @param requestId The id of the Access Watchpoint Request
   *
   * @return True if the access watchpoint request was removed (if it existed),
   *         otherwise false
   */
  def removeAccessWatchpointRequestWithId(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}

