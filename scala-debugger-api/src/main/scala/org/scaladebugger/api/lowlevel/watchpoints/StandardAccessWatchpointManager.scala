package org.scaladebugger.api.lowlevel.watchpoints

import com.sun.jdi.request.{AccessWatchpointRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.collection.JavaConverters._
import scala.util.{Failure, Try}

/**
 * Represents the manager for access watchpoint requests.
 *
 * @param eventRequestManager The manager used to create access watchpoint
 *                            requests
 * @param classManager The manager used to retrieve information about classes
 *                     and their respective fields
 */
class StandardAccessWatchpointManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager
) extends AccessWatchpointManager with Logging {
  import org.scaladebugger.api.lowlevel.requests.Implicits._

  private val accessWatchpointRequests =
    new MultiMap[AccessWatchpointRequestInfo, AccessWatchpointRequest]

  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoint request information
   */
  override def accessWatchpointRequestList: Seq[AccessWatchpointRequestInfo] =
    accessWatchpointRequests.keys

  /**
   * Retrieves the list of access watchpoints contained by this manager.
   *
   * @return The collection of access watchpoints by id
   */
  override def accessWatchpointRequestListById: Seq[String] =
    accessWatchpointRequests.ids

  /**
   * Creates a new access watchpoint request for the specified field using the
   * field's name.
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
  ): Try[String] = {
    val fields = classManager.fieldsWithName(className, fieldName)

    if (fields.isEmpty) return Failure(NoFieldFound(className, fieldName))

    val request = Try(eventRequestManager.createAccessWatchpointRequest(
      fields.head,
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      val l = s"$className.$fieldName"
      val i = requestId
      logger.trace(s"Created access watchpoint request for $l with id '$i'")
      accessWatchpointRequests.putWithId(
        requestId,
        AccessWatchpointRequestInfo(
          requestId,
          isPending = false,
          className,
          fieldName,
          extraArguments
        ),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

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
  ): Boolean = {
    accessWatchpointRequests.hasWithKeyPredicate(w =>
      w.className == className && w.fieldName == fieldName
    )
  }

  /**
   * Determines if a access watchpoint request with the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   * @return True if a access watchpoint request with the id exists,
   *         otherwise false
   */
  override def hasAccessWatchpointRequestWithId(id: String): Boolean = {
    accessWatchpointRequests.hasWithId(id)
  }

  /**
   * Returns the collection of access watchpoint requests representing the
   * access watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return Some collection of access watchpoints for the field, or None if
   *         the specified field has no access watchpoints
   */
  override def getAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Option[Seq[AccessWatchpointRequest]] = {
    val requests = accessWatchpointRequests.getWithKeyPredicate(w =>
      w.className == className && w.fieldName == fieldName
    )

    if (requests.nonEmpty) Some(requests) else None
  }

  /**
   * Retrieves the access watchpoint request using the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   * @return Some access watchpoint request if it exists, otherwise None
   */
  override def getAccessWatchpointRequestWithId(
    id: String
  ): Option[AccessWatchpointRequest] = {
    accessWatchpointRequests.getWithId(id)
  }

  /**
   * Returns the information for an access watchpoint request with the
   * specified id.
   *
   * @param requestId The id of the request
   * @return Some access watchpoint information if found, otherwise None
   */
  override def getAccessWatchpointRequestInfoWithId(
    requestId: String
  ): Option[AccessWatchpointRequestInfo] = {
    accessWatchpointRequestList.find(_.requestId == requestId)
  }

  /**
   * Removes the access watchpoint for the specified field.
   *
   * @param className The name of the class containing the field
   * @param fieldName The name of the field to watch
   * @return True if successfully removed access watchpoint, otherwise false
   */
  override def removeAccessWatchpointRequest(
    className: String,
    fieldName: String
  ): Boolean = {
    val ids = accessWatchpointRequests.getIdsWithKeyPredicate(w =>
      w.className == className && w.fieldName == fieldName
    )

    ids.nonEmpty && ids.forall(removeAccessWatchpointRequestWithId)
  }

  /**
   * Removes the access watchpoint request with the specified id.
   *
   * @param id The id of the Access Watchpoint Request
   * @return True if the access watchpoint request was removed (if it existed),
   *         otherwise false
   */
  override def removeAccessWatchpointRequestWithId(id: String): Boolean = {
    val request = accessWatchpointRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}

