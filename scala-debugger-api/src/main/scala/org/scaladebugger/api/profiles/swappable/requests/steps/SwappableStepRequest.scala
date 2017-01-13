package org.scaladebugger.api.profiles.swappable.requests.steps

import com.sun.jdi.event.StepEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.steps.StepRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfo
import org.scaladebugger.api.profiles.traits.requests.steps.StepRequest

import scala.concurrent.Future
import scala.util.Try

/**
 * Represents a swappable profile for step events that redirects the
 * invocation to another profile.
 */
trait SwappableStepRequest extends StepRequest {
  this: SwappableDebugProfileManagement =>

  override def stepIntoLineWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepIntoLineWithData(threadInfo, extraArguments: _*)
  }

  override def stepOverLineWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverLineWithData(threadInfo, extraArguments: _*)
  }

  override def stepOutLineWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutLineWithData(threadInfo, extraArguments: _*)
  }

  override def stepIntoMinWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepIntoMinWithData(threadInfo, extraArguments: _*)
  }

  override def stepOverMinWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverMinWithData(threadInfo, extraArguments: _*)
  }

  override def stepOutMinWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutMinWithData(threadInfo, extraArguments: _*)
  }

  override def tryCreateStepListenerWithData(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[(StepEventInfo, Seq[JDIEventDataResult])]] = {
    withCurrentProfile.tryCreateStepListenerWithData(threadInfo, extraArguments: _*)
  }

  override def isStepRequestPending(
    threadInfo: ThreadInfo
  ): Boolean = {
    withCurrentProfile.isStepRequestPending(threadInfo)
  }

  override def isStepRequestWithArgsPending(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isStepRequestWithArgsPending(
      threadInfo,
      extraArguments: _*
    )
  }

  override def removeStepRequests(
    threadInfo: ThreadInfo
  ): Seq[StepRequestInfo] = {
    withCurrentProfile.removeStepRequests(threadInfo)
  }

  override def removeStepRequestWithArgs(
    threadInfo: ThreadInfo,
    extraArguments: JDIArgument*
  ): Option[StepRequestInfo] = {
    withCurrentProfile.removeStepRequestWithArgs(
      threadInfo,
      extraArguments: _*
    )
  }

  override def removeAllStepRequests(): Seq[StepRequestInfo] = {
    withCurrentProfile.removeAllStepRequests()
  }

  override def stepRequests: Seq[StepRequestInfo] = {
    withCurrentProfile.stepRequests
  }
}
