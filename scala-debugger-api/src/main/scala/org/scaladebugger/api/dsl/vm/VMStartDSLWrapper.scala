package org.scaladebugger.api.dsl.vm

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMStartEventInfo
import org.scaladebugger.api.profiles.traits.requests.vm.VMStartRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param vmStartProfile The profile to wrap
 */
class VMStartDSLWrapper private[dsl] (
  private val vmStartProfile: VMStartRequest
) {
  /** Represents a VMStart event and any associated data. */
  type VMStartEventAndData = (VMStartEventInfo, Seq[JDIEventDataResult])

  /** @see VMStartRequest#tryGetOrCreateVMStartRequest(JDIArgument*) */
  def onVMStart(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMStartEventInfo]] =
    vmStartProfile.tryGetOrCreateVMStartRequest(extraArguments: _*)

  /** @see VMStartRequest#getOrCreateVMStartRequest(JDIArgument*) */
  def onUnsafeVMStart(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMStartEventInfo] =
    vmStartProfile.getOrCreateVMStartRequest(extraArguments: _*)

  /** @see VMStartRequest#getOrCreateVMStartRequestWithData(JDIArgument*) */
  def onUnsafeVMStartWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMStartEventAndData] =
    vmStartProfile.getOrCreateVMStartRequestWithData(extraArguments: _*)

  /** @see VMStartRequest#tryGetOrCreateVMStartRequestWithData(JDIArgument*) */
  def onVMStartWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMStartEventAndData]] =
    vmStartProfile.tryGetOrCreateVMStartRequestWithData(extraArguments: _*)
}
