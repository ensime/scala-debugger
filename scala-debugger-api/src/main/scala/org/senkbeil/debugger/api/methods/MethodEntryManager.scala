package org.senkbeil.debugger.api.methods

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.MethodEntryRequest
import org.senkbeil.debugger.api.jdi.JDIHelperMethods
import org.senkbeil.debugger.api.jdi.requests.JDIRequestArgument
import org.senkbeil.debugger.api.jdi.requests.filters.ClassInclusionFilter
import org.senkbeil.debugger.api.jdi.requests.properties.{SuspendPolicyProperty, EnabledProperty}
import org.senkbeil.debugger.api.utils.LogLike

import org.senkbeil.debugger.api.requests.Implicits._
import scala.collection.JavaConverters._

import scala.util.{Failure, Success, Try}

/**
 * Represents the manager for method entry requests.
 *
 * @param _virtualMachine The virtual machine whose method entry requests to
 *                        manage
 */
class MethodEntryManager(
  protected val _virtualMachine: VirtualMachine
) extends JDIHelperMethods with LogLike {
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  type MethodEntryKey = (String, String)
  private val methodEntryRequests =
    new ConcurrentHashMap[MethodEntryKey, MethodEntryRequest]()

  /**
   * Retrieves the list of method entry requests contained by this manager.
   *
   * @return The collection of method entry requests in the form of
   *         (class name, method name)
   */
  def methodEntryList: Seq[MethodEntryKey] =
    methodEntryRequests.keySet().asScala.toSeq

  /**
   * Sets the method entry request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method entry event.
   *
   * @param className The name of the class whose method entry events to watch
   * @param methodName The name of the method whose entry to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return True if successful in creating the request, otherwise false
   */
  def setMethodEntry(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Boolean = {
    val request = Try(eventRequestManager.createMethodEntryRequest(
      Seq(
        ClassInclusionFilter(classPattern = className),
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    request match {
      case Success(r) =>
        methodEntryRequests.put((className, methodName), r)
        true
      case Failure(_) =>
        false
    }
  }

  /**
   * Determines if a method entry request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if a method entry request exists, otherwise false
   */
  def hasMethodEntry(className: String, methodName: String): Boolean = {
    methodEntryRequests.containsKey((className, methodName))
  }

  /**
   * Retrieves the method entry request for the specific class and method.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return Some method entry request if it exists, otherwise None
   */
  def getMethodEntry(
    className: String,
    methodName: String
  ): Option[MethodEntryRequest] = {
    Option(methodEntryRequests.get((className, methodName)))
  }

  /**
   * Removes the specified method entry request.
   *
   * @param className The name of the class targeted by the method entry request
   * @param methodName The name of the method targeted by the method entry
   *                   request
   *
   * @return True if the method entry request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodEntry(className: String, methodName: String): Boolean = {
    val request = Option(methodEntryRequests.remove((className, methodName)))

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
