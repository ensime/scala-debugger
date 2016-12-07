package org.scaladebugger.api.dsl.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitedEventInfo
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorWaitedRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param monitorWaitedProfile The profile to wrap
 */
class MonitorWaitedDSLWrapper private[dsl] (
  private val monitorWaitedProfile: MonitorWaitedRequest
) {
  /** Represents a MonitorWaited event and any associated data. */
  type MonitorWaitedEventAndData = (MonitorWaitedEventInfo, Seq[JDIEventDataResult])

  /** @see MonitorWaitedRequest#tryGetOrCreateMonitorWaitedRequest(JDIArgument*) */
  def onMonitorWaited(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventInfo]] =
    monitorWaitedProfile.tryGetOrCreateMonitorWaitedRequest(extraArguments: _*)

  /** @see MonitorWaitedRequest#getOrCreateMonitorWaitedRequest(JDIArgument*) */
  def onUnsafeMonitorWaited(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventInfo] =
    monitorWaitedProfile.getOrCreateMonitorWaitedRequest(extraArguments: _*)

  /** @see MonitorWaitedRequest#getOrCreateMonitorWaitedRequestWithData(JDIArgument*) */
  def onUnsafeMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorWaitedEventAndData] =
    monitorWaitedProfile.getOrCreateMonitorWaitedRequestWithData(
      extraArguments: _*
    )

  /** @see MonitorWaitedRequest#tryGetOrCreateMonitorWaitedRequestWithData(JDIArgument*) */
  def onMonitorWaitedWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorWaitedEventAndData]] =
    monitorWaitedProfile.tryGetOrCreateMonitorWaitedRequestWithData(
      extraArguments: _*
    )
}
