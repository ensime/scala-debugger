package org.scaladebugger.api.dsl.methods

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.MethodEntryEventInfo
import org.scaladebugger.api.profiles.traits.requests.methods.MethodEntryRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param methodEntryProfile The profile to wrap
 */
class MethodEntryDSLWrapper private[dsl] (
  private val methodEntryProfile: MethodEntryRequest
) {
  /** Represents a MethodEntry event and any associated data. */
  type MethodEntryEventAndData = (MethodEntryEventInfo, Seq[JDIEventDataResult])

  /** @see MethodEntryRequest#tryGetOrCreateMethodEntryRequest(String, String, JDIArgument*) */
  def onMethodEntry(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[MethodEntryEventInfo]] =
    methodEntryProfile.tryGetOrCreateMethodEntryRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodEntryRequest#getOrCreateMethodEntryRequest(String, String, JDIArgument*) */
  def onUnsafeMethodEntry(
    className: String,
    methodName: String,
    extraArguments: JDIArgument*
  ): IdentityPipeline[MethodEntryEventInfo] =
    methodEntryProfile.getOrCreateMethodEntryRequest(
      className,
      methodName,
      extraArguments: _*
    )

  /** @see MethodEntryRequest#getOrCreateMethodEntryRequestWithData(String, String, JDIArgument*) */
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

  /** @see MethodEntryRequest#tryGetOrCreateMethodEntryRequestWithData(String, String, JDIArgument*) */
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
