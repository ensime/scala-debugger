package org.scaladebugger.api.profiles.swappable.requests.watchpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.watchpoints.AccessWatchpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.watchpoints.AccessWatchpointRequest

import scala.util.Try

/**
 * Represents a swappable profile for access watchpoint events that redirects
 * the invocation to another profile.
 */
trait SwappableAccessWatchpointRequest extends AccessWatchpointRequest {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateAccessWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] = {
    withCurrentProfile.tryGetOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def isAccessWatchpointRequestPending(
    className: String,
    fieldName: String
  ): Boolean = {
    withCurrentProfile.isAccessWatchpointRequestPending(className, fieldName)
  }

  override def isAccessWatchpointRequestWithArgsPending(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isAccessWatchpointRequestWithArgsPending(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def removeAccessWatchpointRequests(
    className: String,
    fieldName: String
  ): Seq[AccessWatchpointRequestInfo] = {
    withCurrentProfile.removeAccessWatchpointRequests(
      className,
      fieldName
    )
  }

  override def removeAccessWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Option[AccessWatchpointRequestInfo] = {
    withCurrentProfile.removeAccessWatchpointRequestWithArgs(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def removeAllAccessWatchpointRequests(): Seq[AccessWatchpointRequestInfo] = {
    withCurrentProfile.removeAllAccessWatchpointRequests()
  }

  override def accessWatchpointRequests: Seq[AccessWatchpointRequestInfo] = {
    withCurrentProfile.accessWatchpointRequests
  }
}
