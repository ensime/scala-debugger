package org.scaladebugger.api.dsl.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnterEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnterProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param monitorContendedEnterProfile The profile to wrap
 */
class MonitorContendedEnterDSLWrapper private[dsl] (
  private val monitorContendedEnterProfile: MonitorContendedEnterProfile
) {
  /** Represents a MonitorContendedEnter event and any associated data. */
  type MonitorContendedEnterEventAndData =
    (MonitorContendedEnterEventInfoProfile, Seq[JDIEventDataResult])

  /** @see MonitorContendedEnterProfile#tryGetOrCreateMonitorContendedEnterRequest(JDIArgument*) */
  def onMonitorContendedEnter(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventInfoProfile]] =
    monitorContendedEnterProfile.tryGetOrCreateMonitorContendedEnterRequest(extraArguments: _*)

  /** @see MonitorContendedEnterProfile#getOrCreateMonitorContendedEnterRequest(JDIArgument*) */
  def onUnsafeMonitorContendedEnter(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnterEventInfoProfile] =
    monitorContendedEnterProfile.getOrCreateMonitorContendedEnterRequest(extraArguments: _*)

  /** @see MonitorContendedEnterProfile#getOrCreateMonitorContendedEnterRequestWithData(JDIArgument*) */
  def onUnsafeMonitorContendedEnterWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnterEventAndData] =
    monitorContendedEnterProfile.getOrCreateMonitorContendedEnterRequestWithData(
      extraArguments: _*
    )

  /** @see MonitorContendedEnterProfile#tryGetOrCreateMonitorContendedEnterRequestWithData(JDIArgument*) */
  def onMonitorContendedEnterWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] =
    monitorContendedEnterProfile.tryGetOrCreateMonitorContendedEnterRequestWithData(
      extraArguments: _*
    )
}
