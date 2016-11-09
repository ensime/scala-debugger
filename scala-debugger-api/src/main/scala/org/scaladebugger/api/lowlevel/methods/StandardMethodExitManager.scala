package org.scaladebugger.api.lowlevel.methods
import acyclic.file
import com.sun.jdi.request.{EventRequestManager, MethodExitRequest}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{Logging, MultiMap}

import scala.util.{Failure, Try}

/**
 * Represents the manager for method exit requests.
 *
 * @param eventRequestManager The manager used to create method exit requests
 * @param classManager The class manager associated with the virtual machine,
 *                      used to retrieve class and method information
 */
class StandardMethodExitManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager
) extends MethodExitManager with Logging {
  private val methodExitRequests =
    new MultiMap[MethodExitRequestInfo, MethodExitRequest]

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit request information
   */
  override def methodExitRequestList: Seq[MethodExitRequestInfo] =
    methodExitRequests.keys

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit requests by id
   */
  override def methodExitRequestListById: Seq[String] = methodExitRequests.ids

  /**
   * Creates a new method exit request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method exit event.
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class whose method exit events to watch
   * @param methodName The name of the method whose exit to watch
   * @param extraArguments Any additional arguments to provide to the request
   * @return Success(id) if successful, otherwise Failure
   */
  override def createMethodExitRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    // Exit early if class or method not found
    if (classManager.hasMethodWithName(className, methodName))
      return Failure(NoClassMethodFound(className, methodName))

    val arguments = Seq(
      ClassInclusionFilter(classPattern = className),
      EnabledProperty(value = true),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments

    val request = Try(eventRequestManager.createMethodExitRequest(
      arguments: _*
    ))

    if (request.isSuccess) {
      val m = s"$className.$methodName"
      val i = requestId
      logger.trace(s"Created method exit request for $m with id '$i'")
      methodExitRequests.putWithId(
        requestId,
        MethodExitRequestInfo(
          requestId,
          isPending = false,
          className,
          methodName,
          extraArguments
        ),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a method exit request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   * @return True if a method exit request exists, otherwise false
   */
  override def hasMethodExitRequest(
    className: String,
    methodName: String
  ): Boolean = {
    methodExitRequests.hasWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )
  }

  /**
   * Determines if a method exit request exists with the specified id.
   *
   * @param requestId The id of the request
   * @return True if a method exit request exists, otherwise false
   */
  override def hasMethodExitRequestWithId(requestId: String): Boolean = {
    methodExitRequests.hasWithId(requestId)
  }

  /**
   * Retrieves the method exit requests for the specific class and method.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   * @return Some collection of method exit requests if they exist,
   *         otherwise None
   */
  override def getMethodExitRequest(
    className: String,
    methodName: String
  ): Option[Seq[MethodExitRequest]] = {
    val requests = methodExitRequests.getWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )

    if (requests.nonEmpty) Some(requests) else None
  }

  /**
   * Retrieves the method exit request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method exit request if it exists, otherwise None
   */
  override def getMethodExitRequestWithId(
    requestId: String
  ): Option[MethodExitRequest] = {
    methodExitRequests.getWithId(requestId)
  }

  /**
   * Returns the information for a method exit request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method exit information if found, otherwise None
   */
  override def getMethodExitRequestInfoWithId(
    requestId: String
  ): Option[MethodExitRequestInfo] = {
    methodExitRequestList.find(_.requestId == requestId)
  }

  /**
   * Removes the specified method exit request.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodExitRequest(
    className: String,
    methodName: String
  ): Boolean = {
    val ids = methodExitRequests.getIdsWithKeyPredicate(m =>
      m.className == className && m.methodName == methodName
    )

    ids.nonEmpty && ids.forall(removeMethodExitRequestWithId)
  }

  /**
   * Removes the specified method exit request.
   *
   * @param requestId The id of the request
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodExitRequestWithId(
    requestId: String
  ): Boolean = {
    // Remove request with given id
    val request = methodExitRequests.removeWithId(requestId)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}

