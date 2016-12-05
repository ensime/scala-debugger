package org.scaladebugger.api.profiles.swappable.requests.steps

import com.sun.jdi.event.StepEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.lowlevel.steps.StepRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.api.profiles.traits.info.events.StepEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.steps.StepProfile

import scala.concurrent.Future
import scala.util.Try

/**
 * Represents a swappable profile for step events that redirects the
 * invocation to another profile.
 */
trait SwappableStepProfile extends StepProfile {
  this: SwappableDebugProfileManagement =>

  override def stepIntoLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepIntoLineWithData(threadInfoProfile, extraArguments: _*)
  }

  override def stepOverLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverLineWithData(threadInfoProfile, extraArguments: _*)
  }

  override def stepOutLineWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutLineWithData(threadInfoProfile, extraArguments: _*)
  }

  override def stepIntoMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepIntoMinWithData(threadInfoProfile, extraArguments: _*)
  }

  override def stepOverMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOverMinWithData(threadInfoProfile, extraArguments: _*)
  }

  override def stepOutMinWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Future[StepEventAndData] = {
    withCurrentProfile.stepOutMinWithData(threadInfoProfile, extraArguments: _*)
  }

  override def tryCreateStepListenerWithData(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[(StepEventInfoProfile, Seq[JDIEventDataResult])]] = {
    withCurrentProfile.tryCreateStepListenerWithData(threadInfoProfile, extraArguments: _*)
  }

  override def isStepRequestPending(
    threadInfoProfile: ThreadInfoProfile
  ): Boolean = {
    withCurrentProfile.isStepRequestPending(threadInfoProfile)
  }

  override def isStepRequestWithArgsPending(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isStepRequestWithArgsPending(
      threadInfoProfile,
      extraArguments: _*
    )
  }

  override def removeStepRequests(
    threadInfoProfile: ThreadInfoProfile
  ): Seq[StepRequestInfo] = {
    withCurrentProfile.removeStepRequests(threadInfoProfile)
  }

  override def removeStepRequestWithArgs(
    threadInfoProfile: ThreadInfoProfile,
    extraArguments: JDIArgument*
  ): Option[StepRequestInfo] = {
    withCurrentProfile.removeStepRequestWithArgs(
      threadInfoProfile,
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
