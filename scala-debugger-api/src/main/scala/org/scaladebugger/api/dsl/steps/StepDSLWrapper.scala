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
 * @param stepRequest The step request to wrap
 */
class StepDSLWrapper private[dsl] (
  private val stepRequest: StepRequest
) {
  /** Represents a Step event and any associated data. */
  type StepStepAndData = (StepEventInfo, Seq[JDIEventDataResult])

  /** @see StepRequest#tryCreateStepListener(ThreadInfo, JDIArgument*) */
  def onStep(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepEventInfo]] =
    stepRequest.tryCreateStepListener(threadInfo, extraArguments: _*)

  /** @see StepRequest#createStepListener(ThreadInfo, JDIArgument*) */
  def onUnsafeStep(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepEventInfo] =
    stepRequest.createStepListener(threadInfo, extraArguments: _*)

  /** @see StepRequest#createStepListenerWithData(ThreadInfo, JDIArgument*) */
  def onUnsafeStepWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): IdentityPipeline[StepStepAndData] =
    stepRequest.createStepListenerWithData(threadInfo, extraArguments: _*)

  /** @see StepRequest#tryCreateStepListenerWithData(ThreadInfo, JDIArgument*) */
  def onStepWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[StepStepAndData]] =
    stepRequest.tryCreateStepListenerWithData(threadInfo, extraArguments: _*)
}
