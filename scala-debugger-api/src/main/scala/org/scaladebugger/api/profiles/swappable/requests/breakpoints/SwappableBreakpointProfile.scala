package org.scaladebugger.api.profiles.swappable.requests.breakpoints

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.breakpoints.BreakpointRequestInfo
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfileManagement
import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointProfile

import scala.util.Try

/**
 * Represents a swappable profile for breakpoints that redirects the invocation
 * to another profile.
 */
trait SwappableBreakpointProfile extends BreakpointProfile {
  this: SwappableDebugProfileManagement =>

  override def tryGetOrCreateBreakpointRequestWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventAndData]] = {
    withCurrentProfile.tryGetOrCreateBreakpointRequestWithData(
      fileName,
      lineNumber,
      extraArguments: _*
    )
  }

  override def isBreakpointRequestPending(
    fileName: String,
    lineNumber: Int
  ): Boolean = {
    withCurrentProfile.isBreakpointRequestPending(fileName, lineNumber)
  }

  override def isBreakpointRequestWithArgsPending(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Boolean = {
    withCurrentProfile.isBreakpointRequestWithArgsPending(
      fileName,
      lineNumber,
      extraArguments: _*
    )
  }

  override def removeBreakpointRequests(
    fileName: String,
    lineNumber: Int
  ): Seq[BreakpointRequestInfo] = {
    withCurrentProfile.removeBreakpointRequests(fileName, lineNumber)
  }

  override def removeBreakpointRequestWithArgs(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Option[BreakpointRequestInfo] = {
    withCurrentProfile.removeBreakpointRequestWithArgs(
      fileName,
      lineNumber,
      extraArguments: _*
    )
  }

  override def removeAllBreakpointRequests(): Seq[BreakpointRequestInfo] = {
    withCurrentProfile.removeAllBreakpointRequests()
  }

  override def breakpointRequests: Seq[BreakpointRequestInfo] = {
    withCurrentProfile.breakpointRequests
  }
}
