package org.scaladebugger.api.lowlevel.watchpoints
import acyclic.file

import com.sun.jdi.request.{ModificationWatchpointRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{Logging, MultiMap}

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

/**
 * Represents the manager for modification watchpoint requests.
 *
 * @param eventRequestManager The manager used to create modification watchpoint
 *                            requests
 * @param classManager The manager used to retrieve information about classes
 *                     and their respective fields
 */
class StandardModificationWatchpointManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager
) extends ModificationWatchpointManager with Logging {
  import org.scaladebugger.api.lowlevel.requests.Implicits._

  private val modificationWatchpointRequests =
    new MultiMap[ModificationWatchpointRequestInfo, ModificationWatchpointRequest]

  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoint request information
   */
  override def modificationWatchpointRequestList: Seq[ModificationWatchpointRequestInfo] =
    modificationWatchpointRequests.keys

  /**
   * Retrieves the list of modification watchpoints contained by this manager.
   *
   * @return The collection of modification watchpoints by id
   */
  override def modificationWatchpointRequestListById: Seq[String] =
    modificationWatchpointRequests.ids

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
  ): Try[String] = {
    val classReferenceType = classManager.allClasses.find(_.name() == className)
    val field = classReferenceType.flatMap(
      _.allFields().asScala.find(_.name() == fieldName)
    )

    if (field.isEmpty) return Failure(NoFieldFound(className, fieldName))

    val request = Try(eventRequestManager.createModificationWatchpointRequest(
      field.get,
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) modificationWatchpointRequests.putWithId(
      requestId,
      ModificationWatchpointRequestInfo(
        requestId,
        isPending = false,
        className,
        fieldName,
        extraArguments
      ),
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

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
  ): Boolean = {
    modificationWatchpointRequests.hasWithKeyPredicate(w =>
      w.className == className && w.fieldName == fieldName
    )
  }

  /**
   * Determines if a modification watchpoint request with the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   * @return True if a modification watchpoint request with the id exists,
   *         otherwise false
   */
  override def hasModificationWatchpointRequestWithId(id: String): Boolean = {
    modificationWatchpointRequests.hasWithId(id)
  }

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
  ): Option[Seq[ModificationWatchpointRequest]] = {
    val requests = modificationWatchpointRequests.getWithKeyPredicate(w =>
      w.className == className && w.fieldName == fieldName
    )

    if (requests.nonEmpty) Some(requests) else None
  }

  /**
   * Retrieves the modification watchpoint request using the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   * @return Some modification watchpoint request if it exists, otherwise None
   */
  override def getModificationWatchpointRequestWithId(
    id: String
  ): Option[ModificationWatchpointRequest] = {
    modificationWatchpointRequests.getWithId(id)
  }

  /**
   * Returns the information for a modification watchpoint request with the
   * specified id.
   *
   * @param requestId The id of the request
   * @return Some modification watchpoint information if found, otherwise None
   */
  override def getModificationWatchpointRequestInfoWithId(
    requestId: String
  ): Option[ModificationWatchpointRequestInfo] = {
    modificationWatchpointRequestList.find(_.requestId == requestId)
  }

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
  ): Boolean = {
    val ids = modificationWatchpointRequests.getIdsWithKeyPredicate(w =>
      w.className == className && w.fieldName == fieldName
    )

    ids.nonEmpty && ids.forall(removeModificationWatchpointRequestWithId)
  }

  /**
   * Removes the modification watchpoint request with the specified id.
   *
   * @param id The id of the Modification Watchpoint Request
   * @return True if the modification watchpoint request was removed
   *         (if it existed), otherwise false
   */
  override def removeModificationWatchpointRequestWithId(id: String): Boolean = {
    val request = modificationWatchpointRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}

