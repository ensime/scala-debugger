package org.scaladebugger.api.profiles.swappable.requests.monitors

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.monitors.MonitorContendedEnterRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.monitors.MonitorContendedEnterRequest

import scala.util.Try

/**
 * Represents a swappable profile for monitor contended enter events that
 * redirects the invocation to another profile.
 */
trait SwappableMonitorContendedEnterRequest extends MonitorContendedEnterRequest {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateMonitorContendedEnterRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MonitorContendedEnterEventAndData]] = {
    withCurrentProfile.tryGetOrCreateMonitorContendedEnterRequestWithData(extraArguments: _*)
  }

  override def isMonitorContendedEnterRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isMonitorContendedEnterRequestWithArgsPending(extraArguments: _*)
  }

  override def removeMonitorContendedEnterRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[MonitorContendedEnterRequestInfo] = {
    withCurrentProfile.removeMonitorContendedEnterRequestWithArgs(extraArguments: _*)
  }

  override def removeAllMonitorContendedEnterRequests(): Seq[MonitorContendedEnterRequestInfo] = {
    withCurrentProfile.removeAllMonitorContendedEnterRequests()
  }

  override def monitorContendedEnterRequests: Seq[MonitorContendedEnterRequestInfo] = {
    withCurrentProfile.monitorContendedEnterRequests
  }
}
