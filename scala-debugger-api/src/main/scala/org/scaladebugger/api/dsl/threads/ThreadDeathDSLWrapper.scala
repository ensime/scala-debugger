package org.scaladebugger.api.dsl.threads

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.ThreadDeathEventInfo
import org.scaladebugger.api.profiles.traits.requests.threads.ThreadDeathRequest

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param threadDeathProfile The profile to wrap
 */
class ThreadDeathDSLWrapper private[dsl] (
  private val threadDeathProfile: ThreadDeathRequest
) {
  /** Represents a ThreadDeath event and any associated data. */
  type ThreadDeathEventAndData = (ThreadDeathEventInfo, Seq[JDIEventDataResult])

  /** @see ThreadDeathRequest#tryGetOrCreateThreadDeathRequest(JDIArgument*) */
  def onThreadDeath(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventInfo]] =
    threadDeathProfile.tryGetOrCreateThreadDeathRequest(extraArguments: _*)

  /** @see ThreadDeathRequest#getOrCreateThreadDeathRequest(JDIArgument*) */
  def onUnsafeThreadDeath(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadDeathEventInfo] =
    threadDeathProfile.getOrCreateThreadDeathRequest(extraArguments: _*)

  /** @see ThreadDeathRequest#getOrCreateThreadDeathRequestWithData(JDIArgument*) */
  def onUnsafeThreadDeathWithData(
    extraArguments: JDIArgument*
  ): IdentityPipeline[ThreadDeathEventAndData] =
    threadDeathProfile.getOrCreateThreadDeathRequestWithData(
      extraArguments: _*
    )

  /** @see ThreadDeathRequest#tryGetOrCreateThreadDeathRequestWithData(JDIArgument*) */
  def onThreadDeathWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventAndData]] =
    threadDeathProfile.tryGetOrCreateThreadDeathRequestWithData(
      extraArguments: _*
    )
}
