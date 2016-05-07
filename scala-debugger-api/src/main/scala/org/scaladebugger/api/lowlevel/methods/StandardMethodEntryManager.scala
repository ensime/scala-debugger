package org.scaladebugger.api.lowlevel.methods
import acyclic.file

import com.sun.jdi.request.{EventRequestManager, MethodEntryRequest}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for method entry requests.
 *
 * @param eventRequestManager The manager used to create method entry requests
 */
class StandardMethodEntryManager(
  private val eventRequestManager: EventRequestManager
) extends MethodEntryManager with Logging {
  private val methodEntryRequests =
    new MultiMap[MethodEntryRequestInfo, MethodEntryRequest]

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry request information
   */
  override def methodEntryRequestList: Seq[MethodEntryRequestInfo] =
    methodEntryRequests.keys

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry requests by id
   */
  override def methodEntryRequestListById: Seq[String] = methodEntryRequests.ids

  /**
   * Creates a new method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMethodEntryRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createMethodEntryRequest(
      Seq(
        ClassInclusionFilter(classPattern = className),
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) methodEntryRequests.putWithId(
      requestId,
      MethodEntryRequestInfo(
        requestId,
        isPending = false,
        className,
        methodName,
        extraArguments
      ),
      request.get
    )

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a method entry request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   * @return True if a method entry request exists, otherwise false
   */
  override def hasMethodEntryRequest(
    className: String,
    methodName: String
  ): Boolean = {
    methodEntryRequests.hasWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )
  }

  /**
   * Determines if a method entry request exists with the specified id.
   *
   * @param requestId The id of the request
   * @return True if a method entry request exists, otherwise false
   */
  override def hasMethodEntryRequestWithId(requestId: String): Boolean = {
    methodEntryRequests.hasWithId(requestId)
  }

  /**
   * Retrieves the method entry requests for the specific class and method.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   * @return Some collection of method entry requests if they exist,
   *         otherwise None
   */
  override def getMethodEntryRequest(
    className: String,
    methodName: String
  ): Option[Seq[MethodEntryRequest]] = {
    val requests = methodEntryRequests.getWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )

    if (requests.nonEmpty) Some(requests) else None
  }

  /**
   * Retrieves the method entry request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method entry request if it exists, otherwise None
   */
  override def getMethodEntryRequestWithId(
    requestId: String
  ): Option[MethodEntryRequest] = {
    methodEntryRequests.getWithId(requestId)
  }

  /**
   * Returns the information for a method entry request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method entry information if found, otherwise None
   */
  override def getMethodEntryRequestInfoWithId(
    requestId: String
  ): Option[MethodEntryRequestInfo] = {
    methodEntryRequestList.find(_.requestId == requestId)
  }

  /**
   * Removes the specified method entry request.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodEntryRequest(
    className: String,
    methodName: String
  ): Boolean = {
    val ids = methodEntryRequests.getIdsWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )

    ids.nonEmpty && ids.forall(removeMethodEntryRequestWithId)
  }

  /**
   * Removes the specified method entry request.
   *
   * @param requestId The id of the request
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodEntryRequestWithId(
    requestId: String
  ): Boolean = {
    // Remove request with given id
    val request = methodEntryRequests.removeWithId(requestId)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
