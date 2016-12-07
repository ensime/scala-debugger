package org.scaladebugger.api.dsl.watchpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ModificationWatchpointEventInfo
import org.scaladebugger.api.profiles.traits.requests.watchpoints.ModificationWatchpointRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param modificationWatchpointProfile The profile to wrap
 */
class ModificationWatchpointDSLWrapper private[dsl] (
  private val modificationWatchpointProfile: ModificationWatchpointRequest
) {
  /** Represents a ModificationWatchpoint event and any associated data. */
  type ModificationWatchpointEventAndData =
    (ModificationWatchpointEventInfo, Seq[JDIEventDataResult])

  /** @see ModificationWatchpointRequest#tryGetOrCreateModificationWatchpointRequest(String, String, JDIArgument*) */
  def onModificationWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ModificationWatchpointEventInfo]] =
    modificationWatchpointProfile.tryGetOrCreateModificationWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see ModificationWatchpointRequest#getOrCreateModificationWatchpointRequest(String, String, JDIArgument*) */
  def onUnsafeModificationWatchpoint(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventInfo] =
    modificationWatchpointProfile.getOrCreateModificationWatchpointRequest(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see ModificationWatchpointRequest#getOrCreateModificationWatchpointRequestWithData(String, String, JDIArgument*) */
  def onUnsafeModificationWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[ModificationWatchpointEventAndData] =
    modificationWatchpointProfile.getOrCreateModificationWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )

  /** @see ModificationWatchpointRequest#tryGetOrCreateModificationWatchpointRequestWithData(String, String, JDIArgument*) */
  def onModificationWatchpointWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] =
    modificationWatchpointProfile.tryGetOrCreateModificationWatchpointRequestWithData(
      className,
      fieldName,
      extraArguments: _*
    )
}
