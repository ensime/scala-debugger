package org.scaladebugger.api.dsl.methods

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodExitEventInfo
import org.scaladebugger.api.profiles.traits.requests.methods.MethodExitRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param methodExitProfile The profile to wrap
 */
class MethodExitDSLWrapper private[dsl] (
  private val methodExitProfile: MethodExitRequest
) {
  /** Represents a MethodExit event and any associated data. */
  type MethodExitEventAndData = (MethodExitEventInfo, Seq[JDIEventDataResult])

  /** @see MethodExitRequest#tryGetOrCreateMethodExitRequest(String, String, JDIArgument*) */
  def onMethodExit(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodExitEventInfo]] =
    methodExitProfile.tryGetOrCreateMethodExitRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodExitRequest#getOrCreateMethodExitRequest(String, String, JDIArgument*) */
  def onUnsafeMethodExit(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodExitEventInfo] =
    methodExitProfile.getOrCreateMethodExitRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodExitRequest#getOrCreateMethodExitRequestWithData(String, String, JDIArgument*) */
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

  /** @see MethodExitRequest#tryGetOrCreateMethodExitRequestWithData(String, String, JDIArgument*) */
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
