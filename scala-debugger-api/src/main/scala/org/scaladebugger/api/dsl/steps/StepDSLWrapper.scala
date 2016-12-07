package org.scaladebugger.api.dsl.steps

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfo
import org.scaladebugger.api.profiles.traits.requests.steps.StepRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param stepProfile The profile to wrap
 */
class StepDSLWrapper private[dsl] (
  private val stepProfile: StepRequest
) {
  /** Represents a Step event and any associated data. */
  type StepStepAndData = (StepEventInfo, Seq[JDIEventDataResult])

  /** @see StepRequest#tryCreateStepListener(ThreadInfo, JDIArgument*) */
  def onStep(
    threadInfoProfile: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventInfo]] =
    stepProfile.tryCreateStepListener(threadInfoProfile, extraArguments: _*)

  /** @see StepRequest#createStepListener(ThreadInfo, JDIArgument*) */
  def onUnsafeStep(
    threadInfoProfile: ThreadInfo,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEventInfo] =
    stepProfile.createStepListener(threadInfoProfile, extraArguments: _*)

  /** @see StepRequest#createStepListenerWithData(ThreadInfo, JDIArgument*) */
  def onUnsafeStepWithData(
    threadInfoProfile: ThreadInfo,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepStepAndData] =
    stepProfile.createStepListenerWithData(threadInfoProfile, extraArguments: _*)

  /** @see StepRequest#tryCreateStepListenerWithData(ThreadInfo, JDIArgument*) */
  def onStepWithData(
    threadInfoProfile: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepStepAndData]] =
    stepProfile.tryCreateStepListenerWithData(threadInfoProfile, extraArguments: _*)
}
