package org.scaladebugger.api.profiles.swappable.requests.threads

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.threads.ThreadDeathRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.threads.ThreadDeathRequest

import scala.util.Try

/**
 * Represents a swappable profile for thread death events that redirects the
 * invocation to another profile.
 */
trait SwappableThreadDeathRequest extends ThreadDeathRequest {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateThreadDeathRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventAndData]] = {
    withCurrentProfile.tryGetOrCreateThreadDeathRequestWithData(extraArguments: _*)
  }

  override def isThreadDeathRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isThreadDeathRequestWithArgsPending(extraArguments: _*)
  }

  override def removeThreadDeathRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ThreadDeathRequestInfo] = {
    withCurrentProfile.removeThreadDeathRequestWithArgs(extraArguments: _*)
  }

  override def removeAllThreadDeathRequests(): Seq[ThreadDeathRequestInfo] = {
    withCurrentProfile.removeAllThreadDeathRequests()
  }

  override def threadDeathRequests: Seq[ThreadDeathRequestInfo] = {
    withCurrentProfile.threadDeathRequests
  }
}
