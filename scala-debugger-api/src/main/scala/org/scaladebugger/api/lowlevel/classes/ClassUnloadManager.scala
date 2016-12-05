package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.request.ClassUnloadRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for class unload requests.
 */
trait ClassUnloadManager {
  /**
   * Retrieves the list of class unload requests contained by this manager.
   *
   * @return The collection of class unload requests in the form of ids
   */
  def classUnloadRequestList: Seq[String]

  /**
   * Creates a new class unload request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassUnloadRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new class unload request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassUnloadRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createClassUnloadRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new class unload request based on the specified information.
   *
   * @param classUnloadRequestInfo The information used to create the
   *                                class unload request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassUnloadRequestFromInfo(
    classUnloadRequestInfo: ClassUnloadRequestInfo
  ): Try[String] = createClassUnloadRequestWithId(
    classUnloadRequestInfo.requestId,
    classUnloadRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a class unload request with the specified id.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return True if a class unload request with the id exists, otherwise false
   */
  def hasClassUnloadRequest(requestId: String): Boolean

  /**
   * Retrieves the class unload request using the specified id.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return Some class unload request if it exists, otherwise None
   */
  def getClassUnloadRequest(requestId: String): Option[ClassUnloadRequest]

  /**
   * Retrieves the information for a class unload request with the
   * specified id.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getClassUnloadRequestInfo(
    requestId: String
  ): Option[ClassUnloadRequestInfo]

  /**
   * Removes the specified class unload request.
   *
   * @param requestId The id of the Class Unload Request
   *
   * @return True if the class unload request was removed (if it existed),
   *         otherwise false
   */
  def removeClassUnloadRequest(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String =
    java.util.UUID.randomUUID().toString
}
