package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.request.{ClassPrepareRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{Logging, MultiMap}

import scala.util.Try

/**
 * Represents the manager for class prepare requests.
 */
trait ClassPrepareManager {
  /**
   * Retrieves the list of class prepare requests contained by this manager.
   *
   * @return The collection of class prepare requests in the form of ids
   */
  def classPrepareRequestList: Seq[String]

  /**
   * Creates a new class prepare request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassPrepareRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String]

  /**
   * Creates a new class prepare request.
   *
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassPrepareRequest(
    extraArguments: JDIRequestArgument*
  ): Try[String] = createClassPrepareRequestWithId(
    newRequestId(),
    extraArguments: _*
  )

  /**
   * Creates a new class prepare request based on the specified information.
   *
   * @param classPrepareRequestInfo The information used to create the
   *                                class prepare request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createClassPrepareRequestFromInfo(
    classPrepareRequestInfo: ClassPrepareRequestInfo
  ): Try[String] = createClassPrepareRequestWithId(
    classPrepareRequestInfo.requestId,
    classPrepareRequestInfo.extraArguments: _*
  )

  /**
   * Determines if a class prepare request with the specified id.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return True if a class prepare request with the id exists, otherwise false
   */
  def hasClassPrepareRequest(requestId: String): Boolean

  /**
   * Retrieves the class prepare request using the specified id.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return Some class prepare request if it exists, otherwise None
   */
  def getClassPrepareRequest(requestId: String): Option[ClassPrepareRequest]

  /**
   * Retrieves the information for a class prepare request with the
   * specified id.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  def getClassPrepareRequestInfo(
    requestId: String
  ): Option[ClassPrepareRequestInfo]

  /**
   * Removes the specified class prepare request.
   *
   * @param requestId The id of the Class Prepare Request
   *
   * @return True if the class prepare request was removed (if it existed),
   *         otherwise false
   */
  def removeClassPrepareRequest(requestId: String): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String =
    java.util.UUID.randomUUID().toString
}
