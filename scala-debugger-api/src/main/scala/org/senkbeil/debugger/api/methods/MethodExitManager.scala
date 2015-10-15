package org.senkbeil.debugger.api.methods

import java.util.concurrent.ConcurrentHashMap

import com.sun.jdi.VirtualMachine
import com.sun.jdi.request.MethodExitRequest
import org.senkbeil.debugger.api.jdi.JDIHelperMethods
import org.senkbeil.debugger.api.jdi.requests.JDIRequestArgument
import org.senkbeil.debugger.api.jdi.requests.filters.ClassInclusionFilter
import org.senkbeil.debugger.api.jdi.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.senkbeil.debugger.api.requests.Implicits._
import org.senkbeil.debugger.api.utils.LogLike

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
 * Represents the manager for method exit requests.
 *
 * @param _virtualMachine The virtual machine whose method exit requests to
 *                        manage
 */
class MethodExitManager(
  protected val _virtualMachine: VirtualMachine
) extends JDIHelperMethods with LogLike {
  private val eventRequestManager = _virtualMachine.eventRequestManager()

  type MethodExitKey = (String, String)
  private val methodExitRequests =
    new ConcurrentHashMap[MethodExitKey, MethodExitRequest]()

  /**
   * Retrieves the list of method exit requests contained by this manager.
   *
   * @return The collection of method exit requests in the form of
   *         (class name, method name)
   */
  def methodExitList: Seq[MethodExitKey] =
    methodExitRequests.keySet().asScala.toSeq

  /**
   * Sets the method exit request for the specified class and method.
   *
   * @note The method name is purely used for indexing the request in the
   *       internal list. You should set a method name filter on the event
   *       handler for the method exit event.
   *
   * @param className The name of the class whose method exit events to watch
   * @param methodName The name of the method whose exit to watch
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return True if successful in creating the request, otherwise false
   */
  def setMethodExit(
    className: String,
    methodName: String,
    extraArguments: JDIRequestArgument*
  ): Boolean = {
    val request = Try(eventRequestManager.createMethodExitRequest(
      Seq(
        ClassInclusionFilter(classPattern = className),
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    request match {
      case Success(r) =>
        methodExitRequests.put((className, methodName), r)
        true
      case Failure(_) =>
        false
    }
  }

  /**
   * Determines if a method exit request for the specific class and method
   * exists.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return True if a method exit request exists, otherwise false
   */
  def hasMethodExit(className: String, methodName: String): Boolean = {
    methodExitRequests.containsKey((className, methodName))
  }

  /**
   * Retrieves the method exit request for the specific class and method.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return Some method exit request if it exists, otherwise None
   */
  def getMethodExit(
    className: String,
    methodName: String
  ): Option[MethodExitRequest] = {
    Option(methodExitRequests.get((className, methodName)))
  }

  /**
   * Removes the specified method exit request.
   *
   * @param className The name of the class targeted by the method exit request
   * @param methodName The name of the method targeted by the method exit
   *                   request
   *
   * @return True if the method exit request was removed (if it existed),
   *         otherwise false
   */
  def removeMethodExit(className: String, methodName: String): Boolean = {
    val request = Option(methodExitRequests.remove((className, methodName)))

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }
}
