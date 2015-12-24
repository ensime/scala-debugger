package org.scaladebugger.api.profiles.swappable.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.monitors.MonitorContendedEnteredProfile

import scala.util.Try

/**
 * Represents a swappable profile for monitor contended entered events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorContendedEnteredProfile extends MonitorContendedEnteredProfile {
  this: SwappableDebugProfile =>

  override def onMonitorContendedEnteredWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]] = {
    withCurrentProfile.onMonitorContendedEnteredWithData(extraArguments: _*)
  }
}