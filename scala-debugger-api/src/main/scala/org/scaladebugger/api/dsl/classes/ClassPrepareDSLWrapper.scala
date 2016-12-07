package org.scaladebugger.api.dsl.classes

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.classes.ClassPrepareRequest
import org.scaladebugger.api.profiles.traits.info.events.ClassPrepareEventInfo

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param classPrepareProfile The profile to wrap
 */
class ClassPrepareDSLWrapper private[dsl] (
  private val classPrepareProfile: ClassPrepareRequest
) {
  /** Represents a ClassPrepare event and any associated data. */
  type ClassPrepareEventAndData = (ClassPrepareEventInfo, Seq[JDIEventDataResult])

  /** @see ClassPrepareRequest#tryGetOrCreateClassPrepareRequest(JDIArgument*) */
  def onClassPrepare(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventInfo]] =
    classPrepareProfile.tryGetOrCreateClassPrepareRequest(extraArguments: _*)

  /** @see ClassPrepareRequest#getOrCreateClassPrepareRequest(JDIArgument*) */
  def onUnsafeClassPrepare(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventInfo] =
    classPrepareProfile.getOrCreateClassPrepareRequest(extraArguments: _*)

  /** @see ClassPrepareRequest#getOrCreateClassPrepareRequestWithData(JDIArgument*) */
  def onUnsafeClassPrepareWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventAndData] =
    classPrepareProfile.getOrCreateClassPrepareRequestWithData(
      extraArguments: _*
    )

  /** @see ClassPrepareRequest#tryGetOrCreateClassPrepareRequestWithData(JDIArgument*) */
  def onClassPrepareWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]] =
    classPrepareProfile.tryGetOrCreateClassPrepareRequestWithData(
      extraArguments: _*
    )
}
