package org.scaladebugger.api.lowlevel.methods

import com.sun.jdi.request.MethodEntryRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a method entry manager whose operations do nothing.
 */
class DummyMethodEntryManager extends MethodEntryManager {
  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry requests by id
   */
  override def methodEntryRequestListById: Seq[String] = Nil

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
  ): Try[String] = Failure(new DummyOperationException)

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
  ): Option[Seq[MethodEntryRequest]] = None

  /**
   * Returns the information for a method entry request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method entry information if found, otherwise None
   */
  override def getMethodEntryRequestInfoWithId(
    requestId: String
  ): Option[MethodEntryRequestInfo] = None

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry request information
   */
  override def methodEntryRequestList: Seq[MethodEntryRequestInfo] = Nil

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
  ): Boolean = false

  /**
   * Removes the specified method entry request.
   *
   * @param requestId The id of the request
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  override def removeMethodEntryRequestWithId(
    requestId: String
  ): Boolean = false

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
  ): Boolean = false

  /**
   * Determines if a method entry request exists with the specified id.
   *
   * @param requestId The id of the request
   * @return True if a method entry request exists, otherwise false
   */
  override def hasMethodEntryRequestWithId(requestId: String): Boolean = false

  /**
   * Retrieves the method entry request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some method entry request if it exists, otherwise None
   */
  override def getMethodEntryRequestWithId(
    requestId: String
  ): Option[MethodEntryRequest] = None
}
