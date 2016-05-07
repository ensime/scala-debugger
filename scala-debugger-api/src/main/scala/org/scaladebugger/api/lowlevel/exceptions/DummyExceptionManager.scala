package org.scaladebugger.api.lowlevel.exceptions
import acyclic.file

import com.sun.jdi.request.ExceptionRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents an exception manager whose operations do nothing.
 */
class DummyExceptionManager extends ExceptionManager {
  /**
   * Creates a new exception request for the specified exception class.
   *
   * @note Any exception and its subclass will be watched.
   * @param requestId The id associated with the requests for lookup and removal
   * @param exceptionName The full class name of the exception to watch
   * @param notifyCaught If true, events will be reported when the exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when the exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   * @return Success(id) if successful, otherwise Failure
   */
  override def createExceptionRequestWithId(
    requestId: String,
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception request information
   */
  override def exceptionRequestList: Seq[ExceptionRequestInfo] = Nil

  /**
   * Creates a new exception request to catch all exceptions from the JVM.
   *
   * @note The request id given does not get added to the request id list and
   *       removing by id will not remove this request instance.
   * @param requestId The id associated with the requests for lookup and removal
   * @param notifyCaught If true, events will be reported when any exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when any exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   * @return Success(id) if successful, otherwise Failure
   */
  override def createCatchallExceptionRequestWithId(
    requestId: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception requests by id
   */
  override def exceptionRequestListById: Seq[String] = Nil

  /**
   * Determines if an exception request exists with the specified id.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @return True if a exception request exists, otherwise false
   */
  override def hasExceptionRequestWithId(requestId: String): Boolean = false

  /**
   * Removes the specified exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   * @return True if the exception requests were removed (if they existed),
   *         otherwise false
   */
  override def removeExceptionRequest(exceptionName: String): Boolean = false

  /**
   * Retrieves the collection of exception requests with the specified id.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @return Some collection of exception requests if they exist, otherwise None
   */
  override def getExceptionRequestWithId(
    requestId: String
  ): Option[Seq[ExceptionRequest]] = None

  /**
   * Returns the information for an exception request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some exception information if found, otherwise None
   */
  override def getExceptionRequestInfoWithId(
    requestId: String
  ): Option[ExceptionRequestInfo] = None

  /**
   * Determines if an exception request exists for the specified exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   * @return True if a exception request exists, otherwise false
   */
  override def hasExceptionRequest(exceptionName: String): Boolean = false

  /**
   * Retrieves the collection of exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   * @return Some collection of exception requests if they exist, otherwise None
   */
  override def getExceptionRequest(
    exceptionName: String
  ): Option[Seq[ExceptionRequest]] = None

  /**
   * Removes the exception request with the specified id.
   *
   * @param requestId The id of the request
   * @return True if the exception request was removed (if it existed),
   *         otherwise false
   */
  override def removeExceptionRequestWithId(requestId: String): Boolean = false
}
