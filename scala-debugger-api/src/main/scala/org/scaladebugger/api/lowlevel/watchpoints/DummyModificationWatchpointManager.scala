package org.scaladebugger.api.lowlevel.watchpoints
import acyclic.file

import com.sun.jdi.request.ModificationWatchpointRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a modification watchpoint manager whose operations do nothing.
 */
class DummyModificationWatchpointManager extends ModificationWatchpointManager {
  /**
   * Determines if a modification watchpoint request with the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return True if a modification watchpoint request with the id exists,
   *         otherwise false
   */
  override def hasModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = false

  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoint request information
   */
  override def modificationWatchpointRequestList: Seq[ModificationWatchpointRequestInfo] = Nil

  /**
   * Returns the collection of modification watchpoint requests representing the
   * modification watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return Some collection of modification watchpoints for the field,
   *         or None if the specified field has no modification watchpoints
   */
  override def getModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Option[Seq[ModificationWatchpointRequest]] = None

  /**
   * Removes the modification watchpoint request with the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   * @return True if the modification watchpoint request was removed
   *         (if it existed), otherwise false
   */
  override def removeModificationWatchpointRequestWithId(
    id: String
  ): Boolean = false

  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoints by id
   */
  override def modificationWatchpointRequestListById: Seq[String] = Nil

  /**
   * Removes the modification watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return True if successfully removed modification watchpoint,
   *         otherwise false
   */
  override def removeModificationWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = false

  /**
   * Determines if a modification watchpoint request with the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   * @return True if a modification watchpoint request with the id exists,
   *         otherwise false
   */
  override def hasModificationWatchpointRequestWithId(
    id: String
  ): Boolean = false

  /**
   * Retrieves the modification watchpoint request using the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   * @return Some modification watchpoint request if it exists, otherwise None
   */
  override def getModificationWatchpointRequestWithId(
    id: String
  ): Option[ModificationWatchpointRequest] = None

  /**
   * Returns the information for a modification watchpoint request with the
   * specified id.
   *
   * @param requestId The id of the request
   * @return Some modification watchpoint information if found, otherwise None
   */
  override def getModificationWatchpointRequestInfoWithId(
    requestId: String
  ): Option[ModificationWatchpointRequestInfo] = None

  /**
   * Creates a new modification watchpoint request for the specified field
   * using the field's name.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @param extraArguments Any additional arguments to provide to the request
   * @return Success(id) if successful, otherwise Failure
   */
  override def createModificationWatchpointRequestWithId(
    requestId: String,
    className: String,
    fieldName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)
}
