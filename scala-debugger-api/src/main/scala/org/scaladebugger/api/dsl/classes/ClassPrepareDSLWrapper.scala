package org.scaladebugger.api.dsl.classes

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.classes.ClassPrepareProfile
import org.scaladebugger.api.profiles.traits.info.events.ClassPrepareEventInfoProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param classPrepareProfile The profile to wrap
 */
class ClassPrepareDSLWrapper private[dsl] (
  private val classPrepareProfile: ClassPrepareProfile
) {
  /** Represents a ClassPrepare event and any associated data. */
  type ClassPrepareEventAndData = (ClassPrepareEventInfoProfile, Seq[JDIEventDataResult])

  /** @see ClassPrepareProfile#tryGetOrCreateClassPrepareRequest(JDIArgument*) */
  def onClassPrepare(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventInfoProfile]] =
    classPrepareProfile.tryGetOrCreateClassPrepareRequest(extraArguments: _*)

  /** @see ClassPrepareProfile#getOrCreateClassPrepareRequest(JDIArgument*) */
  def onUnsafeClassPrepare(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventInfoProfile] =
    classPrepareProfile.getOrCreateClassPrepareRequest(extraArguments: _*)

  /** @see ClassPrepareProfile#getOrCreateClassPrepareRequestWithData(JDIArgument*) */
  def onUnsafeClassPrepareWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ClassPrepareEventAndData] =
    classPrepareProfile.getOrCreateClassPrepareRequestWithData(
      extraArguments: _*
    )

  /** @see ClassPrepareProfile#tryGetOrCreateClassPrepareRequestWithData(JDIArgument*) */
  def onClassPrepareWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]] =
    classPrepareProfile.tryGetOrCreateClassPrepareRequestWithData(
      extraArguments: _*
    )
}
