package org.scaladebugger.api.profiles.traits.vm
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.vm.VMDeathRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDeathEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * vm death functionality for a specific debug profile.
 */
trait VMDeathProfile {
  /** Represents a vm death event and any associated data. */
  type VMDeathEventAndData =
    (VMDeathEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending vm death requests.
   *
   * @return The collection of information on vm death requests
   */
  def vmDeathRequests: Seq[VMDeathRequestInfo]

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events
   */
  def tryGetOrCreateVMDeathRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventInfoProfile]] = {
    tryGetOrCreateVMDeathRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateVMDeathRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventAndData]]

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events
   */
  def getOrCreateVMDeathRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDeathEventInfoProfile] = {
    tryGetOrCreateVMDeathRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of vm death events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm death events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateVMDeathRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDeathEventAndData] = {
    tryGetOrCreateVMDeathRequestWithData(extraArguments: _*).get
  }

  /**
   * Determines if the vm death request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       vm death request
   * @return True if there is at least one vm death request
   *         with the provided extra arguments that is pending, otherwise false
   */
  def isVMDeathRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all vm death requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       vm death request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeVMDeathRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[VMDeathRequestInfo]

  /**
   * Removes all vm death requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       vm death request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveVMDeathRequestWithArgs(
    extraArguments: JDIArgument*
  ): Try[Option[VMDeathRequestInfo]] = Try(removeVMDeathRequestWithArgs(
    extraArguments: _*
  ))

  /**
   * Removes all vm death requests.
   *
   * @return The collection of information about removed vm death requests
   */
  def removeAllVMDeathRequests(): Seq[VMDeathRequestInfo]

  /**
   * Removes all vm death requests.
   *
   * @return Success containing the collection of information about removed
   *         vm death requests, otherwise a failure
   */
  def tryRemoveAllVMDeathRequests(): Try[Seq[VMDeathRequestInfo]] = Try(
    removeAllVMDeathRequests()
  )
}
