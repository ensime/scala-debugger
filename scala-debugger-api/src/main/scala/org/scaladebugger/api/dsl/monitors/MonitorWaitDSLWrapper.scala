package org.scaladebugger.api.dsl.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorWaitProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param monitorWaitProfile The profile to wrap
 */
class MonitorWaitDSLWrapper private[dsl] (
  private val monitorWaitProfile: MonitorWaitProfile
) {
  /** Represents a MonitorWait event and any associated data. */
  type MonitorWaitEventAndData = (MonitorWaitEventInfoProfile, Seq[JDIEventDataResult])

  /** @see MonitorWaitProfile#tryGetOrCreateMonitorWaitRequest(JDIArgument*) */
  def onMonitorWait(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventInfoProfile]] =
    monitorWaitProfile.tryGetOrCreateMonitorWaitRequest(extraArguments: _*)

  /** @see MonitorWaitProfile#getOrCreateMonitorWaitRequest(JDIArgument*) */
  def onUnsafeMonitorWait(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitEventInfoProfile] =
    monitorWaitProfile.getOrCreateMonitorWaitRequest(extraArguments: _*)

  /** @see MonitorWaitProfile#getOrCreateMonitorWaitRequestWithData(JDIArgument*) */
  def onUnsafeMonitorWaitWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitEventAndData] =
    monitorWaitProfile.getOrCreateMonitorWaitRequestWithData(
      extraArguments: _*
    )

  /** @see MonitorWaitProfile#tryGetOrCreateMonitorWaitRequestWithData(JDIArgument*) */
  def onMonitorWaitWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitEventAndData]] =
    monitorWaitProfile.tryGetOrCreateMonitorWaitRequestWithData(
      extraArguments: _*
    )
}
