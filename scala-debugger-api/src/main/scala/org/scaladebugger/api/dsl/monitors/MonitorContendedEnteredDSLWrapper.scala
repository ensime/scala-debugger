package org.scaladebugger.api.dsl.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MonitorContendedEnteredEventInfo
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnteredRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param monitorContendedEnteredProfile The profile to wrap
 */
class MonitorContendedEnteredDSLWrapper private[dsl] (
  private val monitorContendedEnteredProfile: MonitorContendedEnteredRequest
) {
  /** Represents a MonitorContendedEntered event and any associated data. */
  type MonitorContendedEnteredEventAndData =
    (MonitorContendedEnteredEventInfo, Seq[JDIEventDataResult])

  /** @see MonitorContendedEnteredRequest#tryGetOrCreateMonitorContendedEnteredRequest(JDIArgument*) */
  def onMonitorContendedEntered(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventInfo]] =
    monitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequest(extraArguments: _*)

  /** @see MonitorContendedEnteredRequest#getOrCreateMonitorContendedEnteredRequest(JDIArgument*) */
  def onUnsafeMonitorContendedEntered(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnteredEventInfo] =
    monitorContendedEnteredProfile.getOrCreateMonitorContendedEnteredRequest(extraArguments: _*)

  /** @see MonitorContendedEnteredRequest#getOrCreateMonitorContendedEnteredRequestWithData(JDIArgument*) */
  def onUnsafeMonitorContendedEnteredWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[MonitorContendedEnteredEventAndData] =
    monitorContendedEnteredProfile.getOrCreateMonitorContendedEnteredRequestWithData(
      extraArguments: _*
    )

  /** @see MonitorContendedEnteredRequest#tryGetOrCreateMonitorContendedEnteredRequestWithData(JDIArgument*) */
  def onMonitorContendedEnteredWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]] =
    monitorContendedEnteredProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(
      extraArguments: _*
    )
}
