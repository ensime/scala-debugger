package org.scaladebugger.api.lowlevel.watchpoints

import com.sun.jdi.request.ModificationWatchpointRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for modification watchpoint requests.
 */
trait ModificationWatchpointManager {
  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoint request information
   */
  def modificationWatchpointRequestList: Seq[ModificationWatchpointRequestInfo]

  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoints by id
   */
  def modificationWatchpointRequestListById: Seq[String]

  /**
   * Creates a new modification watchpoint request for the specified field
   * using the field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createModificationWatchpointRequestWithId(
    requestId: String,
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new modification watchpoint request for the specified field
   * using the field's name.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createModificationWatchpointRequest(
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createModificationWatchpointRequestWithId(
    newRequestId(),
    className,
    fieldName,
    extraArguments: _*
  )

  /**
   * Creates a modification watchpoint request based on the specified
   * information.
   *
   * @param modificationWatchpointRequestInfo The information used to create the
   *                                          modification watchpoint request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createModificationWatchpointRequestFromInfo(
    modificationWatchpointRequestInfo: ModificationWatchpointRequestInfo
  ): Try[String] = createModificationWatchpointRequestWithId(
    modificationWatchpointRequestInfo.requestId,
    modificationWatchpointRequestInfo.className,
    modificationWatchpointRequestInfo.fieldName,
    modificationWatchpointRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a modification watchpoint request with the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if a modification watchpoint request with the id exists,
   *         otherwise false
   */
  def hasModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean

  /**
   * Determines if a modification watchpoint request with the specified id.
   *
   * @param requestId The id of the Modification Watchpoint Request
   *
   * @return True if a modification watchpoint request with the id exists,
   *         otherwise false
   */
  def hasModificationWatchpointRequestWithId(requestId: String): Boolean

  /**
   * Returns the collection of modification watchpoint requests representing the
   * modification watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return Some collection of modification watchpoints for the field,
   *         or None if the specified field has no modification watchpoints
   */
  def getModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Option[Seq[ModificationWatchpointRequest]]

  /**
   * Retrieves the modification watchpoint request using the specified id.
   *
   * @param requestId The id of the Modification Watchpoint Request
   *
   * @return Some modification watchpoint request if it exists, otherwise None
   */
  def getModificationWatchpointRequestWithId(
    requestId: String
  ): Option[ModificationWatchpointRequest]

  /**
   * Returns the information for a modification watchpoint request with the
   * specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some modification watchpoint information if found, otherwise None
   */
  def getModificationWatchpointRequestInfoWithId(
    requestId: String
  ): Option[ModificationWatchpointRequestInfo]

  /**
   * Removes the modification watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   *
   * @return True if successfully removed modification watchpoint,
   *         otherwise false
   */
  def removeModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean

  /**
   * Removes the modification watchpoint request with the specified id.
   *
   * @param requestId The id of the Modification Watchpoint Request
   *
   * @return True if the modification watchpoint request was removed
   *         (if it existed), otherwise false
   */
  def removeModificationWatchpointRequestWithId(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}

