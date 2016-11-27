package org.scaladebugger.api.profiles.traits.vm
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDisconnectEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * vm disconnect functionality for a specific debug profile.
 */
trait VMDisconnectProfile {
  /** Represents a vm disconnect event and any associated data. */
  type VMDisconnectEventAndData =
    (VMDisconnectEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Constructs a stream of vm disconnect events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm disconnect events
   */
  def tryGetOrCreateVMDisconnectRequest(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventInfoProfile]] = {
    tryGetOrCreateVMDisconnectRequestWithData(extraArguments: _*).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of vm disconnect events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm disconnect events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateVMDisconnectRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventAndData]]

  /**
   * Constructs a stream of vm disconnect events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm disconnect events
   */
  def getOrCreateVMDisconnectRequest(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDisconnectEventInfoProfile] = {
    tryGetOrCreateVMDisconnectRequest(extraArguments: _*).get
  }

  /**
   * Constructs a stream of vm disconnect events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of vm disconnect events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateVMDisconnectRequestWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDisconnectEventAndData] = {
    tryGetOrCreateVMDisconnectRequestWithData(extraArguments: _*).get
  }
}
