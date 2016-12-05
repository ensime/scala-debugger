package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.request.ClassPrepareRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a class prepare manager whose operations do nothing.
 */
class DummyClassPrepareManager extends ClassPrepareManager {
  /**
   * Determines if a class prepare request with the specified id.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return True if a class prepare request with the id exists,
   *         otherwise false
   */
  override def hasClassPrepareRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the class prepare request using the specified id.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return Some class prepare request if it exists, otherwise None
   */
  override def getClassPrepareRequest(
    requestId: String
  ): Option[ClassPrepareRequest] = None

  /**
   * Retrieves the information for a class prepare request with the
   * specified id.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getClassPrepareRequestInfo(
    requestId: String
  ): Option[ClassPrepareRequestInfo] = None

  /**
   * Retrieves the list of class prepare requests contained by
   * this manager.
   *
   * @return The collection of class prepare requests in the form of
   *         ids
   */
  override def classPrepareRequestList: Seq[String] = Nil

  /**
   * Removes the specified class prepare request.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return True if the class prepare request was removed
   *         (if it existed), otherwise false
   */
  override def removeClassPrepareRequest(
    requestId: String
  ): Boolean = false

  /**
   * Creates a new class prepare request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createClassPrepareRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)
}
