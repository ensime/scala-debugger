package org.scaladebugger.api.dsl.methods

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodExitEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.methods.MethodExitProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param methodExitProfile The profile to wrap
 */
class MethodExitDSLWrapper private[dsl] (
  private val methodExitProfile: MethodExitProfile
) {
  /** Represents a MethodExit event and any associated data. */
  type MethodExitEventAndData = (MethodExitEventInfoProfile, Seq[JDIEventDataResult])

  /** @see MethodExitProfile#tryGetOrCreateMethodExitRequest(String, String, JDIArgument*) */
  def onMethodExit(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventInfoProfile]] =
    methodExitProfile.tryGetOrCreateMethodExitRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodExitProfile#getOrCreateMethodExitRequest(String, String, JDIArgument*) */
  def onUnsafeMethodExit(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEventInfoProfile] =
    methodExitProfile.getOrCreateMethodExitRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodExitProfile#getOrCreateMethodExitRequestWithData(String, String, JDIArgument*) */
  def onUnsafeMethodExitWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEventAndData] =
    methodExitProfile.getOrCreateMethodExitRequestWithData(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodExitProfile#tryGetOrCreateMethodExitRequestWithData(String, String, JDIArgument*) */
  def onMethodExitWithData(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventAndData]] =
    methodExitProfile.tryGetOrCreateMethodExitRequestWithData(
      className,
      methodName,
      extraArguments: _*
    )
}
