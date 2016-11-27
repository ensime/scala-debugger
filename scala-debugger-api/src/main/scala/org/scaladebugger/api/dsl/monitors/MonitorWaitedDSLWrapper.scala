package org.scaladebugger.api.dsl.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitedEventInfoProfile
import org.scaladebugger.api.profiles.traits.monitors.MonitorWaitedProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param monitorWaitedProfile The profile to wrap
 */
class MonitorWaitedDSLWrapper private[dsl] (
  private val monitorWaitedProfile: MonitorWaitedProfile
) {
  /** Represents a MonitorWaited event and any associated data. */
  type MonitorWaitedEventAndData = (MonitorWaitedEventInfoProfile, Seq[JDIEventDataResult])

  /** @see MonitorWaitedProfile#tryGetOrCreateMonitorWaitedRequest(JDIArgument*) */
  def onMonitorWaited(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventInfoProfile]] =
    monitorWaitedProfile.tryGetOrCreateMonitorWaitedRequest(extraArguments: _*)

  /** @see MonitorWaitedProfile#getOrCreateMonitorWaitedRequest(JDIArgument*) */
  def onUnsafeMonitorWaited(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventInfoProfile] =
    monitorWaitedProfile.getOrCreateMonitorWaitedRequest(extraArguments: _*)

  /** @see MonitorWaitedProfile#getOrCreateMonitorWaitedRequestWithData(JDIArgument*) */
  def onUnsafeMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventAndData] =
    monitorWaitedProfile.getOrCreateMonitorWaitedRequestWithData(
      extraArguments: _*
    )

  /** @see MonitorWaitedProfile#tryGetOrCreateMonitorWaitedRequestWithData(JDIArgument*) */
  def onMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventAndData]] =
    monitorWaitedProfile.tryGetOrCreateMonitorWaitedRequestWithData(
      extraArguments: _*
    )
}
