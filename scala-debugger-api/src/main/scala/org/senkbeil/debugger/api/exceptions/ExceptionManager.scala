package org.senkbeil.debugger.api.exceptions

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.{ReferenceType, VirtualMachine}
import com.sun.jdi.request.ExceptionRequest
import org.senkbeil.debugger.api.jdi.JDIHelperMethods
import org.senkbeil.debugger.api.jdi.requests.JDIRequestArgument
import org.senkbeil.debugger.api.jdi.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.senkbeil.debugger.api.utils.LogLike
import scala.collection.JavaConverters._
import org.senkbeil.debugger.api.requests.Implicits._

import scala.util.{Failure, Success, Try}

/**
 * Represents the manager for exception requests.
 *
 * @param _virtualMachine The virtual machine whose exception requests to
 *                        manage
 */
class ExceptionManager(
  protected val _virtualMachine: VirtualMachine
) extends JDIHelperMethods with LogLike {
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  type ExceptionKey = String
  private val exceptionRequests =
    new ConcurrentHashMap[ExceptionKey, Seq[ExceptionRequest]]()

  @volatile
  private var catchallExceptionRequest: Option[ExceptionRequest] = None

  /**
   * Retrieves the list of exception requests contained by this manager.
   *
   * @return The collection of exception requests by full exception class name
   */
  def exceptionList: Seq[ExceptionKey] =
    exceptionRequests.keySet().asScala.toSeq

  /**
   * Sets the exception request to catch all exceptions from the JVM.
   *
   * @param notifyCaught If true, events will be reported when any exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when any exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return True if successful in creating the request, otherwise false
   */
  def setCatchallException(
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Boolean = {
    val arguments = Seq(
      EnabledProperty(value = true),
      SuspendPolicyProperty.EventThread
    ) ++ extraArguments

    val request = Try(eventRequestManager.createExceptionRequest(
      null, notifyCaught, notifyUncaught, arguments: _*
    ))

    request match {
      case Success(r) =>
        catchallExceptionRequest = Some(r)
        true
      case Failure(_) =>
        false
    }
  }

  /**
   * Determines if the exception request to catch all exceptions has been set.
   *
   * @return True if set, otherwise false
   */
  def hasCatchallException: Boolean = catchallExceptionRequest.nonEmpty

  /**
   * Retrieves the exception request used to catch all exceptions.
   *
   * @return Some exception request if the catchall has been set, otherwise None
   */
  def getCatchallException: Option[ExceptionRequest] = catchallExceptionRequest

  /**
   * Removes the exception request used to catch all exceptions.
   *
   * @return True if the exception request was removed (if it existed),
   *         otherwise false
   */
  def removeCatchallException(): Boolean =
    catchallExceptionRequest.synchronized {
      catchallExceptionRequest match {
        case Some(r) =>
          eventRequestManager.deleteEventRequest(r)
          catchallExceptionRequest = None
          true
        case None =>
          false
      }
    }

  /**
   * Sets the exception request for the specified exception class.
   *
   * @note Any exception and its subclass will be watched.
   *
   * @param exceptionName The full class name of the exception to watch
   * @param notifyCaught If true, events will be reported when the exception
   *                     is detected within a try { ... } block
   * @param notifyUncaught If true, events will be reported when the exception
   *                       is detected not within a try { ... } block
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return True if successful in creating the request, otherwise false
   */
  def setException(
    exceptionName: String,
    notifyCaught: Boolean,
    notifyUncaught: Boolean,
    extraArguments: JDIRequestArgument*
  ): Boolean = {
    val exceptionReferenceTypes = _virtualMachine.classesByName(exceptionName)
    logger.info(s"Found classes for $exceptionName: ${exceptionReferenceTypes.asScala.mkString(",")}")

    // If no classes match the requested exception type, exit early
    if (exceptionReferenceTypes.isEmpty) return false

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

    requests match {
      case Success(r) =>
        exceptionRequests.put(exceptionName, r)
        true
      case Failure(_) =>
        false
    }
  }

  /**
   * Determines if an exception request exists for the specified exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   *
   * @return True if a exception request exists, otherwise false
   */
  def hasException(exceptionName: String): Boolean = {
    exceptionRequests.containsKey(exceptionName)
  }

  /**
   * Retrieves the collection of exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   *
   * @return Some collection of exception requests if they exist, otherwise None
   */
  def getException(exceptionName: String): Option[Seq[ExceptionRequest]] = {
    Option(exceptionRequests.get(exceptionName))
  }

  /**
   * Removes the specified exception requests with the matching exception
   * class name.
   *
   * @param exceptionName The full class name of the exception targeted by the
   *                      exception requests
   *
   * @return True if the exception requests were removed (if they existed),
   *         otherwise false
   */
  def removeException(exceptionName: String): Boolean = {
    val requests = Option(exceptionRequests.remove(exceptionName))

    requests.foreach(_.foreach(eventRequestManager.deleteEventRequest))

    requests.nonEmpty
  }
}
