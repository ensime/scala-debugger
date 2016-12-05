package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.request.ClassUnloadRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a class unload manager whose operations do nothing.
 */
class DummyClassUnloadManager extends ClassUnloadManager {
  /**
   * Determines if a class unload request with the specified id.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return True if a class unload request with the id exists,
   *         otherwise false
   */
  override def hasClassUnloadRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the class unload request using the specified id.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return Some class unload request if it exists, otherwise None
   */
  override def getClassUnloadRequest(
    requestId: String
  ): Option[ClassUnloadRequest] = None

  /**
   * Retrieves the information for a class unload request with the
   * specified id.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getClassUnloadRequestInfo(
    requestId: String
  ): Option[ClassUnloadRequestInfo] = None

  /**
   * Retrieves the list of class unload requests contained by
   * this manager.
   *
   * @return The collection of class unload requests in the form of
   *         ids
   */
  override def classUnloadRequestList: Seq[String] = Nil

  /**
   * Removes the specified class unload request.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return True if the class unload request was removed
   *         (if it existed), otherwise false
   */
  override def removeClassUnloadRequest(
    requestId: String
  ): Boolean = false

  /**
   * Creates a new class unload request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createClassUnloadRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)
}
