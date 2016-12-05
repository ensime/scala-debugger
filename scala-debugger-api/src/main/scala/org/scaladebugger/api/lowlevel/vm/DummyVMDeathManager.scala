package org.scaladebugger.api.lowlevel.vm

import com.sun.jdi.request.VMDeathRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a vm death manager whose operations do nothing.
 */
class DummyVMDeathManager extends VMDeathManager {
  /**
   * Determines if a vm death request with the specified id.
   *
   * @param requestId The id of the VM Death Request
   *
   * @return True if a vm death request with the id exists,
   *         otherwise false
   */
  override def hasVMDeathRequest(
    requestId: String
  ): Boolean = false

  /**
   * Retrieves the vm death request using the specified id.
   *
   * @param requestId The id of the VM Death Request
   *
   * @return Some vm death request if it exists, otherwise None
   */
  override def getVMDeathRequest(
    requestId: String
  ): Option[VMDeathRequest] = None

  /**
   * Retrieves the information for a vm death request with the
   * specified id.
   *
   * @param requestId The id of the VM Death Request
   *
   * @return Some information about the request if it exists, otherwise None
   */
  override def getVMDeathRequestInfo(
    requestId: String
  ): Option[VMDeathRequestInfo] = None

  /**
   * Retrieves the list of vm death requests contained by
   * this manager.
   *
   * @return The collection of vm death requests in the form of
   *         ids
   */
  override def vmDeathRequestList: Seq[String] = Nil

  /**
   * Removes the specified vm death request.
   *
   * @param requestId The id of the VM Death Request
   *
   * @return True if the vm death request was removed
   *         (if it existed), otherwise false
   */
  override def removeVMDeathRequest(
    requestId: String
  ): Boolean = false

  /**
   * Creates a new vm death request.
   *
   * @param requestId The id of the request used to retrieve and delete it
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  override def createVMDeathRequestWithId(
    requestId: String,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)
}
