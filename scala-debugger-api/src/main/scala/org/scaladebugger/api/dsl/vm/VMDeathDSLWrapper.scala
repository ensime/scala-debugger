package org.scaladebugger.api.dsl.vm

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.VMDeathEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.vm.VMDeathProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param vmDeathProfile The profile to wrap
 */
class VMDeathDSLWrapper private[dsl] (
  private val vmDeathProfile: VMDeathProfile
) {
  /** Represents a VMDeath event and any associated data. */
  type VMDeathEventAndData = (VMDeathEventInfoProfile, Seq[JDIEventDataResult])

  /** @see VMDeathProfile#tryGetOrCreateVMDeathRequest(JDIArgument*) */
  def onVMDeath(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventInfoProfile]] =
    vmDeathProfile.tryGetOrCreateVMDeathRequest(extraArguments: _*)

  /** @see VMDeathProfile#getOrCreateVMDeathRequest(JDIArgument*) */
  def onUnsafeVMDeath(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDeathEventInfoProfile] =
    vmDeathProfile.getOrCreateVMDeathRequest(extraArguments: _*)

  /** @see VMDeathProfile#getOrCreateVMDeathRequestWithData(JDIArgument*) */
  def onUnsafeVMDeathWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[VMDeathEventAndData] =
    vmDeathProfile.getOrCreateVMDeathRequestWithData(extraArguments: _*)

  /** @see VMDeathProfile#tryGetOrCreateVMDeathRequestWithData(JDIArgument*) */
  def onVMDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[VMDeathEventAndData]] =
    vmDeathProfile.tryGetOrCreateVMDeathRequestWithData(extraArguments: _*)
}
