package org.scaladebugger.api.dsl.steps

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.steps.StepProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param stepProfile The profile to wrap
 */
class StepDSLWrapper private[dsl] (
  private val stepProfile: StepProfile
) {
  /** Represents a Step event and any associated data. */
  type StepStepAndData = (StepEventInfoProfile, Seq[JDIEventDataResult])

  /** @see StepProfile#tryCreateStepListener(ThreadInfoProfile, JDIArgument*) */
  def onStep(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventInfoProfile]] =
    stepProfile.tryCreateStepListener(threadInfoProfile, extraArguments: _*)

  /** @see StepProfile#createStepListener(ThreadInfoProfile, JDIArgument*) */
  def onUnsafeStep(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEventInfoProfile] =
    stepProfile.createStepListener(threadInfoProfile, extraArguments: _*)

  /** @see StepProfile#createStepListenerWithData(ThreadInfoProfile, JDIArgument*) */
  def onUnsafeStepWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepStepAndData] =
    stepProfile.createStepListenerWithData(threadInfoProfile, extraArguments: _*)

  /** @see StepProfile#tryCreateStepListenerWithData(ThreadInfoProfile, JDIArgument*) */
  def onStepWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepStepAndData]] =
    stepProfile.tryCreateStepListenerWithData(threadInfoProfile, extraArguments: _*)
}
