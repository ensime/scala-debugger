package org.scaladebugger.api.dsl.vm

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDeathEventInfo
import org.scaladebugger.api.profiles.traits.requests.vm.VMDeathRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param vmDeathProfile The profile to wrap
 */
class VMDeathDSLWrapper private[dsl] (
  private val vmDeathProfile: VMDeathRequest
) {
  /** Represents a VMDeath event and any associated data. */
  type VMDeathEventAndData = (VMDeathEventInfo, Seq[JDIEventDataResult])

  /** @see VMDeathRequest#tryGetOrCreateVMDeathRequest(JDIArgument*) */
  def onVMDeath(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventInfo]] =
    vmDeathProfile.tryGetOrCreateVMDeathRequest(extraArguments: _*)

  /** @see VMDeathRequest#getOrCreateVMDeathRequest(JDIArgument*) */
  def onUnsafeVMDeath(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDeathEventInfo] =
    vmDeathProfile.getOrCreateVMDeathRequest(extraArguments: _*)

  /** @see VMDeathRequest#getOrCreateVMDeathRequestWithData(JDIArgument*) */
  def onUnsafeVMDeathWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDeathEventAndData] =
    vmDeathProfile.getOrCreateVMDeathRequestWithData(extraArguments: _*)

  /** @see VMDeathRequest#tryGetOrCreateVMDeathRequestWithData(JDIArgument*) */
  def onVMDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventAndData]] =
    vmDeathProfile.tryGetOrCreateVMDeathRequestWithData(extraArguments: _*)
}
