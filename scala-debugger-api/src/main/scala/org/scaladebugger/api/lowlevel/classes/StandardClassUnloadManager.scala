package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.request.{EventRequestManager, ClassUnloadRequest}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for class unload requests.
 *
 * @param eventRequestManager The manager used to create class unload requests
 */
class StandardClassUnloadManager(
  private val eventRequestManager: EventRequestManager
) extends ClassUnloadManager with Logging {
  private val classUnloadRequests =
    new MultiMap[ClassUnloadRequestInfo, ClassUnloadRequest]

  /**
   * Retrieves the list of class unload requests contained by this manager.
   *
   * @return The collection of class unload requests in the form of ids
   */
  override def classUnloadRequestList: Seq[String] = classUnloadRequests.ids

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
  ): Try[String] = {
    val request = Try(eventRequestManager.createClassUnloadRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created class unload request with id '$requestId'")
      classUnloadRequests.putWithId(
        requestId,
        ClassUnloadRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a class unload request with the specified id.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if a class unload request with the id exists, otherwise false
   */
  override def hasClassUnloadRequest(id: String): Boolean = {
    classUnloadRequests.hasWithId(id)
  }

  /**
   * Retrieves the class unload request using the specified id.
   *
   * @param id The id of the Class Unload Request
   *
   * @return Some class unload request if it exists, otherwise None
   */
  override def getClassUnloadRequest(id: String): Option[ClassUnloadRequest] = {
    classUnloadRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a class unload request with the
   * specified id.
   *
   * @param id The id of the Class Unload Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getClassUnloadRequestInfo(
    id: String
  ): Option[ClassUnloadRequestInfo] = {
    classUnloadRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified class unload request.
   *
   * @param id The id of the Class Unload Request
   *
   * @return True if the class unload request was removed (if it existed),
   *         otherwise false
   */
  override def removeClassUnloadRequest(id: String): Boolean = {
    val request = classUnloadRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
