package org.scaladebugger.api.lowlevel.methods

import com.sun.jdi.request.MethodExitRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for method exit requests.
 */
trait MethodExitManager {
  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit request information
   */
  def methodExitRequestList: Seq[MethodExitRequestInfo]

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit requests by id
   */
  def methodExitRequestListById: Seq[String]

  /**
   * Creates a new method exit request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method exit event.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param className The name of the class whose method exit events to watch
   * @param methodName The name of the method whose exit to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodExitRequestWithId(
    requestId: String,
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new method exit request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method exit event.
   *
   * @param className The name of the class whose method exit events to watch
   * @param methodName The name of the method whose exit to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodExitRequest(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createMethodExitRequestWithId(
    newRequestId(),
    className,
    methodName,
    extraArguments: _*
  )

  /**
   * Creates a method exit request based on the specified information.
   *
   * @param methodExitRequestInfo The information used to create the
   *                              method exit request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createMethodExitRequestFromInfo(
    methodExitRequestInfo: MethodExitRequestInfo
  ): Try[String] = createMethodExitRequestWithId(
    methodExitRequestInfo.requestId,
    methodExitRequestInfo.className,
    methodExitRequestInfo.methodName,
    methodExitRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a method exit request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return True if a method exit request exists, otherwise false
   */
  def hasMethodExitRequest(className: String, methodName: String): Boolean

  /**
   * Determines if a method exit request exists with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if a method exit request exists, otherwise false
   */
  def hasMethodExitRequestWithId(requestId: String): Boolean

  /**
   * Retrieves the method exit requests for the specific class and method.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return Some collection of method exit requests if they exist,
   *         otherwise None
   */
  def getMethodExitRequest(
    className: String,
    methodName: String
  ): Option[Seq[MethodExitRequest]]

  /**
   * Retrieves the method exit request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some method exit request if it exists, otherwise None
   */
  def getMethodExitRequestWithId(
    requestId: String
  ): Option[MethodExitRequest]

  /**
   * Returns the information for a method exit request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some method exit information if found, otherwise None
   */
  def getMethodExitRequestInfoWithId(
    requestId: String
  ): Option[MethodExitRequestInfo]

  /**
   * Removes the specified method exit request.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodExitRequest(
    className: String,
    methodName: String
  ): Boolean

  /**
   * Removes the specified method exit request.
   *
   * @param requestId The id of the request
   *
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodExitRequestWithId(
    requestId: String
  ): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}

