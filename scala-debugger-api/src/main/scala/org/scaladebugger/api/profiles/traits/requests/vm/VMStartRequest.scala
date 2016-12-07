package org.scaladebugger.api.profiles.traits.requests.vm

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMStartEventInfo

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * vm start functionality for a specific debug profile.
 */
trait VMStartRequest {
  /** Represents a vm start event and any associated data. */
  type VMStartEventAndData =
    (VMStartEventInfo, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of vm start events
   */
  def tryGetOrCreateVMStartRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMStartEventInfo]] = {
    tryGetOrCreateVMStartRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of vm start events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateVMStartRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMStartEventAndData]]

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of vm start events
   */
  def getOrCreateVMStartRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMStartEventInfo] = {
    tryGetOrCreateVMStartRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of vm start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of vm start events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateVMStartRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMStartEventAndData] = {
    tryGetOrCreateVMStartRequestWithData(extraArguments: _*).get
  }
}

