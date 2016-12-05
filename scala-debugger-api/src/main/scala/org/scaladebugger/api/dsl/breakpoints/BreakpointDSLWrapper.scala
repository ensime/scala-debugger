package org.scaladebugger.api.dsl.breakpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointProfile
import org.scaladebugger.api.profiles.traits.info.events.BreakpointEventInfoProfile

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param breakpointProfile The profile to wrap
 */
class BreakpointDSLWrapper private[dsl] (
  private val breakpointProfile: BreakpointProfile
) {
  /** Represents a breakpoint event and any associated data. */
  type BreakpointEventAndData = (BreakpointEventInfoProfile, Seq[JDIEventDataResult])

  /** @see BreakpointProfile#tryGetOrCreateBreakpointRequest(String, Int, JDIArgument*) */
  def onBreakpoint(
    fileName: String, lineNumber: Int, extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventInfoProfile]] =
    breakpointProfile.tryGetOrCreateBreakpointRequest(
      fileName, lineNumber, extraArguments: _*
    )

  /** @see BreakpointProfile#getOrCreateBreakpointRequest(String, Int, JDIArgument*) */
  def onUnsafeBreakpoint(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventInfoProfile] =
    breakpointProfile.getOrCreateBreakpointRequest(
      fileName, lineNumber, extraArguments: _*
    )

  /** @see BreakpointProfile#getOrCreateBreakpointRequestWithData(String, Int, JDIArgument*) */
  def onUnsafeBreakpointWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventAndData] =
    breakpointProfile.getOrCreateBreakpointRequestWithData(
      fileName, lineNumber, extraArguments: _*
    )

  /** @see BreakpointProfile#tryGetOrCreateBreakpointRequestWithData(String, Int, JDIArgument*) */
  def onBreakpointWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventAndData]] =
    breakpointProfile.tryGetOrCreateBreakpointRequestWithData(
      fileName, lineNumber, extraArguments: _*
    )
}
