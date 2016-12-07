package org.scaladebugger.api.dsl.threads

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ThreadStartEventInfo
import org.scaladebugger.api.profiles.traits.requests.threads.ThreadStartRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param threadStartProfile The profile to wrap
 */
class ThreadStartDSLWrapper private[dsl] (
  private val threadStartProfile: ThreadStartRequest
) {
  /** Represents a ThreadStart event and any associated data. */
  type ThreadStartEventAndData = (ThreadStartEventInfo, Seq[JDIEventDataResult])

  /** @see ThreadStartRequest#tryGetOrCreateThreadStartRequest(JDIArgument*) */
  def onThreadStart(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventInfo]] =
    threadStartProfile.tryGetOrCreateThreadStartRequest(extraArguments: _*)

  /** @see ThreadStartRequest#getOrCreateThreadStartRequest(JDIArgument*) */
  def onUnsafeThreadStart(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadStartEventInfo] =
    threadStartProfile.getOrCreateThreadStartRequest(extraArguments: _*)

  /** @see ThreadStartRequest#getOrCreateThreadStartRequestWithData(JDIArgument*) */
  def onUnsafeThreadStartWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadStartEventAndData] =
    threadStartProfile.getOrCreateThreadStartRequestWithData(
      extraArguments: _*
    )

  /** @see ThreadStartRequest#tryGetOrCreateThreadStartRequestWithData(JDIArgument*) */
  def onThreadStartWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventAndData]] =
    threadStartProfile.tryGetOrCreateThreadStartRequestWithData(
      extraArguments: _*
    )
}
