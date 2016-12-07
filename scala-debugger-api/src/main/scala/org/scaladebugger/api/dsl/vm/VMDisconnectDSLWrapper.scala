package org.scaladebugger.api.dsl.vm

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDisconnectEventInfo
import org.scaladebugger.api.profiles.traits.requests.vm.VMDisconnectRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param vmDisconnectProfile The profile to wrap
 */
class VMDisconnectDSLWrapper private[dsl] (
  private val vmDisconnectProfile: VMDisconnectRequest
) {
  /** Represents a VMDisconnect event and any associated data. */
  type VMDisconnectEventAndData = (VMDisconnectEventInfo, Seq[JDIEventDataResult])

  /** @see VMDisconnectRequest#tryGetOrCreateVMDisconnectRequest(JDIArgument*) */
  def onVMDisconnect(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventInfo]] =
    vmDisconnectProfile.tryGetOrCreateVMDisconnectRequest(extraArguments: _*)

  /** @see VMDisconnectRequest#getOrCreateVMDisconnectRequest(JDIArgument*) */
  def onUnsafeVMDisconnect(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDisconnectEventInfo] =
    vmDisconnectProfile.getOrCreateVMDisconnectRequest(extraArguments: _*)

  /** @see VMDisconnectRequest#getOrCreateVMDisconnectRequestWithData(JDIArgument*) */
  def onUnsafeVMDisconnectWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDisconnectEventAndData] =
    vmDisconnectProfile.getOrCreateVMDisconnectRequestWithData(extraArguments: _*)

  /** @see VMDisconnectRequest#tryGetOrCreateVMDisconnectRequestWithData(JDIArgument*) */
  def onVMDisconnectWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDisconnectEventAndData]] =
    vmDisconnectProfile.tryGetOrCreateVMDisconnectRequestWithData(extraArguments: _*)
}
