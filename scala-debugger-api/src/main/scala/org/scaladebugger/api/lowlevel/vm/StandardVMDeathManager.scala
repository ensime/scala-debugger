package org.scaladebugger.api.lowlevel.vm

import com.sun.jdi.request.{EventRequestManager, VMDeathRequest}
import org.scaladebugger.api.lowlevel.requests.Implicits._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}

import scala.util.Try

/**
 * Represents the manager for vm death requests.
 *
 * @param eventRequestManager The manager used to create vm death requests
 */
class StandardVMDeathManager(
  private val eventRequestManager: EventRequestManager
) extends VMDeathManager with Logging {
  private val vmDeathRequests =
    new MultiMap[VMDeathRequestInfo, VMDeathRequest]

  /**
   * Retrieves the list of vm death requests contained by this manager.
   *
   * @return The collection of vm death requests in the form of ids
   */
  override def vmDeathRequestList: Seq[String] = vmDeathRequests.ids

  /**
   * Creates a new vm death request for the specified class and method.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createVMDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    val request = Try(eventRequestManager.createVMDeathRequest(
      Seq(
        EnabledProperty(value = true),
        SuspendPolicyProperty.EventThread
      ) ++ extraArguments: _*
    ))

    if (request.isSuccess) {
      logger.trace(s"Created vm death request with id '$requestId'")
      vmDeathRequests.putWithId(
        requestId,
        VMDeathRequestInfo(requestId, isPending = false, extraArguments),
        request.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    request.map(_ => requestId)
  }

  /**
   * Determines if a vm death request with the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if a vm death request with the id exists, otherwise false
   */
  override def hasVMDeathRequest(id: String): Boolean = {
    vmDeathRequests.hasWithId(id)
  }

  /**
   * Retrieves the vm death request using the specified id.
   *
   * @param id The id of the VM Death Request
   *
   * @return Some vm death request if it exists, otherwise None
   */
  override def getVMDeathRequest(id: String): Option[VMDeathRequest] = {
    vmDeathRequests.getWithId(id)
  }

  /**
   * Retrieves the information for a vm death request with the specified id.
   *
   * @param id The id of the request
   *
   * @return Some vm death information if found, otherwise None
   */
  override def getVMDeathRequestInfo(id: String): Option[VMDeathRequestInfo] = {
    vmDeathRequests.getKeyWithId(id)
  }

  /**
   * Removes the specified vm death request.
   *
   * @param id The id of the VM Death Request
   *
   * @return True if the vm death request was removed (if it existed),
   *         otherwise false
   */
  override def removeVMDeathRequest(id: String): Boolean = {
    val request = vmDeathRequests.removeWithId(id)

    request.foreach(eventRequestManager.deleteEventRequest)

    request.nonEmpty
  }

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected override def newRequestId(): String = java.util.UUID.randomUUID().toString
}

