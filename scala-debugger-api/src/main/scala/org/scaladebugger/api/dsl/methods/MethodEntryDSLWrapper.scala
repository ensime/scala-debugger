package org.scaladebugger.api.dsl.methods

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodEntryEventInfoProfile
import org.scaladebugger.api.profiles.traits.methods.MethodEntryProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param methodEntryProfile The profile to wrap
 */
class MethodEntryDSLWrapper private[dsl] (
  private val methodEntryProfile: MethodEntryProfile
) {
  /** Represents a MethodEntry event and any associated data. */
  type MethodEntryEventAndData = (MethodEntryEventInfoProfile, Seq[JDIEventDataResult])

  /** @see MethodEntryProfile#tryGetOrCreateMethodEntryRequest(String, String, JDIArgument*) */
  def onMethodEntry(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventInfoProfile]] =
    methodEntryProfile.tryGetOrCreateMethodEntryRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodEntryProfile#getOrCreateMethodEntryRequest(String, String, JDIArgument*) */
  def onUnsafeMethodEntry(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodEntryEventInfoProfile] =
    methodEntryProfile.getOrCreateMethodEntryRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodEntryProfile#getOrCreateMethodEntryRequestWithData(String, String, JDIArgument*) */
  def onUnsafeMethodEntryWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodEntryEventAndData] =
    methodEntryProfile.getOrCreateMethodEntryRequestWithData(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodEntryProfile#tryGetOrCreateMethodEntryRequestWithData(String, String, JDIArgument*) */
  def onMethodEntryWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventAndData]] =
    methodEntryProfile.tryGetOrCreateMethodEntryRequestWithData(
      className,
      methodName,
      extraArguments: _*
    )
}
