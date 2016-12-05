package org.scaladebugger.api.profiles.swappable.requests.watchpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.watchpoints.ModificationWatchpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.watchpoints.ModificationWatchpointProfile

import scala.util.Try

/**
 * Represents a swappable profile for modification watchpoint events that
 * redirects the invocation to another profile.
 */
trait SwappableModificationWatchpointProfile extends ModificationWatchpointProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateModificationWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
    withCurrentProfile.tryGetOrCreateModificationWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def isModificationWatchpointRequestPending(
    className: String,
    fieldName: String
  ): Boolean = {
    withCurrentProfile.isModificationWatchpointRequestPending(
      className,
      fieldName
    )
  }

  override def isModificationWatchpointRequestWithArgsPending(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isModificationWatchpointRequestWithArgsPending(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def removeModificationWatchpointRequests(
    className: String,
    fieldName: String
  ): Seq[ModificationWatchpointRequestInfo] = {
    withCurrentProfile.removeModificationWatchpointRequests(
      className,
      fieldName
    )
  }

  override def removeModificationWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Option[ModificationWatchpointRequestInfo] = {
    withCurrentProfile.removeModificationWatchpointRequestWithArgs(
      className,
      fieldName,
      extraArguments: _*
    )
  }

  override def removeAllModificationWatchpointRequests(): Seq[ModificationWatchpointRequestInfo] = {
    withCurrentProfile.removeAllModificationWatchpointRequests()
  }

  override def modificationWatchpointRequests: Seq[ModificationWatchpointRequestInfo] = {
    withCurrentProfile.modificationWatchpointRequests
  }
}
