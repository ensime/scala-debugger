package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.request.{ClassPrepareRequest, EventRequestManager}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for class prepare requests.
 *
 * @param eventRequestManager The manager used to create class prepare requests
 */
class StandardClassPrepareManager(
  private val eventRequestManager: EventRequestManager
) extends ClassPrepareManager with Logging {
  private val classPrepareRequests =
    new MultiMap[ClassPrepareRequestInfo, ClassPrepareRequest]

  /**
   * Retrieves the list of class prepare requests contained by this manager.
   *
   * @return The collection of class prepare requests in the form of ids
   */
  override def classPrepareRequestList: Seq[String] = classPrepareRequests.ids

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
  ): Try[String] = {
    val request = Try(eventRequestManager.createClassPrepareRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.AllThreads
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created class prepare request with id '$requestId'")
      classPrepareRequests.putWithId(
        requestId,
        ClassPrepareRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a class prepare request with the specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return True if a class prepare request with the id exists, otherwise false
   */
  override def hasClassPrepareRequest(id: String): Boolean = {
    classPrepareRequests.hasWithId(id)
  }

  /**
   * Retrieves the class prepare request using the specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return Some class prepare request if it exists, otherwise None
   */
  override def getClassPrepareRequest(
    id: String
  ): Option[ClassPrepareRequest] = {
    classPrepareRequests.getWithId(id)
  }

  /**
   * Retrieves the arguments provided to the class prepare request with the
   * specified id.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return Some collection of arguments if it exists, otherwise None
   */
  override def getClassPrepareRequestInfo(
    id: String
  ): Option[ClassPrepareRequestInfo] = {
    classPrepareRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified class prepare request.
   *
   * @param id The id of the Class Prepare Request
   *
   * @return True if the class prepare request was removed (if it existed),
   *         otherwise false
   */
  override def removeClassPrepareRequest(id: String): Boolean = {
    val request = classPrepareRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
