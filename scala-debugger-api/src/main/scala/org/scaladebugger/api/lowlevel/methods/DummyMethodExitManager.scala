package org.scaladebugger.api.lowlevel.methods

import com.sun.jdi.request.MethodExitRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a method exit manager whose operations do nothing.
 */
class DummyMethodExitManager extends MethodExitManager {
  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit requests by id
   */
  override def methodExitRequestListById: Seq[String] = Nil

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
  ): Try[String] = Failure(new DummyOperationException)

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
  ): Option[Seq[MethodExitRequest]] = None

  /**
   * Returns the information for a method exit request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method exit information if found, otherwise None
   */
  override def getMethodExitRequestInfoWithId(
    requestId: String
  ): Option[MethodExitRequestInfo] = None

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit request information
   */
  override def methodExitRequestList: Seq[MethodExitRequestInfo] = Nil

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
  ): Boolean = false

  /**
   * Removes the specified method exit request.
   *
   * @param requestId The id of the request
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodExitRequestWithId(
    requestId: String
  ): Boolean = false

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
  ): Boolean = false

  /**
   * Determines if a method exit request exists with the specified id.
   *
   * @param requestId The id of the request
   * @return True if a method exit request exists, otherwise false
   */
  override def hasMethodExitRequestWithId(requestId: String): Boolean = false

  /**
   * Retrieves the method exit request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method exit request if it exists, otherwise None
   */
  override def getMethodExitRequestWithId(
    requestId: String
  ): Option[MethodExitRequest] = None
}
