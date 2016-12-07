package org.scaladebugger.api.dsl.watchpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.AccessWatchpointEventInfo
import org.scaladebugger.api.profiles.traits.requests.watchpoints.AccessWatchpointRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param accessWatchpointProfile The profile to wrap
 */
class AccessWatchpointDSLWrapper private[dsl] (
  private val accessWatchpointProfile: AccessWatchpointRequest
) {
  /** Represents a AccessWatchpoint event and any associated data. */
  type AccessWatchpointEventAndData =
    (AccessWatchpointEventInfo, Seq[JDIEventDataResult])

  /** @see AccessWatchpointRequest#tryGetOrCreateAccessWatchpointRequest(String, String, JDIArgument*) */
  def onAccessWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventInfo]] =
    accessWatchpointProfile.tryGetOrCreateAccessWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see AccessWatchpointRequest#getOrCreateAccessWatchpointRequest(String, String, JDIArgument*) */
  def onUnsafeAccessWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[AccessWatchpointEventInfo] =
    accessWatchpointProfile.getOrCreateAccessWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see AccessWatchpointRequest#getOrCreateAccessWatchpointRequestWithData(String, String, JDIArgument*) */
  def onUnsafeAccessWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[AccessWatchpointEventAndData] =
    accessWatchpointProfile.getOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see AccessWatchpointRequest#tryGetOrCreateAccessWatchpointRequestWithData(String, String, JDIArgument*) */
  def onAccessWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[AccessWatchpointEventAndData]] =
    accessWatchpointProfile.tryGetOrCreateAccessWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )
}
