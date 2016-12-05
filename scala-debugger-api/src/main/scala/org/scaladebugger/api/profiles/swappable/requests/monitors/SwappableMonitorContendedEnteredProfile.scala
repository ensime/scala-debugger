package org.scaladebugger.api.profiles.swappable.requests.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnteredRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnteredProfile

import scala.util.Try

/**
 * Represents a swappable profile for monitor contended entered events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorContendedEnteredProfile extends MonitorContendedEnteredProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateMonitorContendedEnteredRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnteredEventAndData]] = {
    withCurrentProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(extraArguments: _*)
  }

  override def isMonitorContendedEnteredRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isMonitorContendedEnteredRequestWithArgsPending(extraArguments: _*)
  }

  override def removeMonitorContendedEnteredRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorContendedEnteredRequestInfo] = {
    withCurrentProfile.removeMonitorContendedEnteredRequestWithArgs(extraArguments: _*)
  }

  override def removeAllMonitorContendedEnteredRequests(): Seq[MonitorContendedEnteredRequestInfo] = {
    withCurrentProfile.removeAllMonitorContendedEnteredRequests()
  }

  override def monitorContendedEnteredRequests: Seq[MonitorContendedEnteredRequestInfo] = {
    withCurrentProfile.monitorContendedEnteredRequests
  }
}
