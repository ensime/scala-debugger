package org.scaladebugger.api.lowlevel.exceptions

import com.sun.jdi.{ReferenceType, VirtualMachine}
import com.sun.jdi.request.{EventRequestManager, ExceptionRequest}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}
import scala.collection.JavaConverters._
import org.scaladebugger.api.lowlevel.requests.Implicits._

import scala.util.{Failure, Try}

/**
 * Represents the manager for exception requests.
 *
 * @param virtualMachine The virtual machine whose classes related to
 *                        exceptions to retrieve
 * @param eventRequestManager The manager used to create exception requests
 */
class StandardExceptionManager(
  private val virtualMachine: VirtualMachine,
  private val eventRequestManager: EventRequestManager
) extends ExceptionManager with Logging {
  private val exceptionRequests =
    new MultiMap[ExceptionRequestInfo, Seq[ExceptionRequest]]

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception request information
   */
  override def exceptionRequestList: Seq[ExceptionRequestInfo] =
    exceptionRequests.keys

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception requests by id
   */
  override def exceptionRequestListById: Seq[String] = exceptionRequests.ids

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
  ): Try[String] = {
    val arguments = Seq(
      EnabledProperty(value = true),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments

    val request = Try(eventRequestManager.createExceptionRequest(
      null, notifyCaught, notifyUncaught, arguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created catchall exception request with id '$requestId'")
      exceptionRequests.putWithId(
        requestId,
        ExceptionRequestInfo(
          requestId = requestId,
          isPending = false,
          className = ExceptionRequestInfo.DefaultCatchallExceptionName,
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          extraArguments = extraArguments
        ),
        Seq(request.get)
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

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
  ): Try[String] = {
    require(exceptionName != null, "Exception name cannot be null!")
    val exceptionReferenceTypes = virtualMachine.classesByName(exceptionName)

    // If no classes match the requested exception type, exit early
    if (exceptionReferenceTypes.isEmpty)
      return Failure(NoExceptionClassFound(exceptionName))

    val arguments = Seq(
      EnabledProperty(value = true),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments

    // TODO: Back out exception creation if a failure occurs
    val requests = Try(exceptionReferenceTypes.asScala.map(
      eventRequestManager.createExceptionRequest(
        _: ReferenceType, notifyCaught, notifyUncaught, arguments: _*
      )
    ))

    if (requests.isSuccess) {
      val en = exceptionName
      logger.trace(s"Created exception request for $en with id '$requestId'")
      exceptionRequests.putWithId(
        requestId,
        ExceptionRequestInfo(
          requestId = requestId,
          isPending = false,
          className = exceptionName,
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          extraArguments = extraArguments
        ),
        requests.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    requests.map(_ => requestId)
  }

  /**
   * Determines if an exception request exists for the specified exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   * @return True if a exception request exists, otherwise false
   */
  override def hasExceptionRequest(exceptionName: String): Boolean = {
    exceptionRequests.hasWithKeyPredicate(_.className == exceptionName)
  }

  /**
   * Determines if an exception request exists with the specified id.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @return True if a exception request exists, otherwise false
   */
  override def hasExceptionRequestWithId(requestId: String): Boolean = {
    exceptionRequests.hasWithId(requestId)
  }

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
  ): Option[Seq[ExceptionRequest]] = {
    val requests = exceptionRequests.getWithKeyPredicate(
      _.className == exceptionName
    ).flatten

    if (requests.nonEmpty) Some(requests) else None
  }

  /**
   * Retrieves the collection of exception requests with the specified id.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @return Some collection of exception requests if they exist, otherwise None
   */
  override def getExceptionRequestWithId(
    requestId: String
  ): Option[Seq[ExceptionRequest]] = {
    exceptionRequests.getWithId(requestId)
  }

  /**
   * Returns the information for an exception request with the specified id.
   *
   * @param requestId The id of the request
   * @return Some exception information if found, otherwise None
   */
  override def getExceptionRequestInfoWithId(
    requestId: String
  ): Option[ExceptionRequestInfo] = {
    exceptionRequestList.find(_.requestId == requestId)
  }

  /**
   * Removes the specified exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   * @return True if the exception requests were removed (if they existed),
   *         otherwise false
   */
  override def removeExceptionRequest(exceptionName: String): Boolean = {
    val ids = exceptionRequests.getIdsWithKeyPredicate(
      _.className == exceptionName
    )

    ids.nonEmpty && ids.forall(removeExceptionRequestWithId)
  }

  /**
   * Removes the exception request with the specified id.
   *
   * @param requestId The id of the request
   * @return True if the exception request was removed (if it existed),
   *         otherwise false
   */
  override def removeExceptionRequestWithId(requestId: String): Boolean = {
    val requests = exceptionRequests.removeWithId(requestId)

    requests.map(_.asJava).foreach(eventRequestManager.deleteEventRequests)

    requests.nonEmpty
  }
}
