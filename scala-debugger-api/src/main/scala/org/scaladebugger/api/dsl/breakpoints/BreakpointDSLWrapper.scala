package org.scaladebugger.api.dsl.breakpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointRequest
import org.scaladebugger.api.profiles.traits.info.events.BreakpointEventInfo

import scala.util.Try

/**
 * Wraps a profile, providing DSL-like syntax.
 *
 * @param breakpointProfile The profile to wrap
 */
class BreakpointDSLWrapper private[dsl] (
  private val breakpointProfile: BreakpointRequest
) {
  /** Represents a breakpoint event and any associated data. */
  type BreakpointEventAndData = (BreakpointEventInfo, Seq[JDIEventDataResult])

  /** @see BreakpointRequest#tryGetOrCreateBreakpointRequest(String, Int, JDIArgument*) */
  def onBreakpoint(
    fileName: String, lineNumber: Int, extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventInfo]] =
    breakpointProfile.tryGetOrCreateBreakpointRequest(
      fileName, lineNumber, extraArguments: _*
    )

  /** @see BreakpointRequest#getOrCreateBreakpointRequest(String, Int, JDIArgument*) */
  def onUnsafeBreakpoint(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventInfo] =
    breakpointProfile.getOrCreateBreakpointRequest(
      fileName, lineNumber, extraArguments: _*
    )

  /** @see BreakpointRequest#getOrCreateBreakpointRequestWithData(String, Int, JDIArgument*) */
  def onUnsafeBreakpointWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventAndData] =
    breakpointProfile.getOrCreateBreakpointRequestWithData(
      fileName, lineNumber, extraArguments: _*
    )

  /** @see BreakpointRequest#tryGetOrCreateBreakpointRequestWithData(String, Int, JDIArgument*) */
  def onBreakpointWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventAndData]] =
    breakpointProfile.tryGetOrCreateBreakpointRequestWithData(
      fileName, lineNumber, extraArguments: _*
    )
}
