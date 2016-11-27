package org.scaladebugger.api.profiles.traits.breakpoints
//import acyclic.file

import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.breakpoints.BreakpointRequestInfo
import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.traits.info.events.BreakpointEventInfoProfile

import scala.util.Try

/**
 * Represents the interface that needs to be implemented to provide
 * breakpoint functionality for a specific debug profile.
 */
trait BreakpointProfile {
  /** Represents a breakpoint event and any associated data. */
  type BreakpointEventAndData = (BreakpointEventInfoProfile, Seq[JDIEventDataResult])

  /**
   * Retrieves the collection of active and pending breakpoints requests.
   *
   * @return The collection of information on breakpoint requests
   */
  def breakpointRequests: Seq[BreakpointRequestInfo]

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of breakpoint events
   */
  def tryGetOrCreateBreakpointRequest(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventInfoProfile]] = {
    tryGetOrCreateBreakpointRequestWithData(
      fileName,
      lineNumber,
      extraArguments: _*
    ).map(_.map(_._1).noop())
  }

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of breakpoint events
   */
  def getOrCreateBreakpointRequest(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventInfoProfile] = {
    tryGetOrCreateBreakpointRequest(
      fileName,
      lineNumber,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of breakpoint events and any retrieved data based on
   *         requests from extra arguments
   */
  def getOrCreateBreakpointRequestWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): IdentityPipeline[BreakpointEventAndData] = {
    tryGetOrCreateBreakpointRequestWithData(
      fileName,
      lineNumber,
      extraArguments: _*
    ).get
  }

  /**
   * Constructs a stream of breakpoint events for the specified file and line
   * number.
   *
   * @param fileName The name of the file where the breakpoint will be set
   * @param lineNumber The line number within the file where the breakpoint
   *                   will be set
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of breakpoint events and any retrieved data based on
   *         requests from extra arguments
   */
  def tryGetOrCreateBreakpointRequestWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventAndData]]

  /**
   * Determines if there is any breakpoint on the specified file and line
   * that is pending.
   *
   * @param fileName The name of the file where the breakpoint resides
   * @param lineNumber The number of the line where the breakpoint resides
   * @return True if there is at least one breakpoint at the specified location
   *         that is pending, otherwise false
   */
  def isBreakpointRequestPending(fileName: String, lineNumber: Int): Boolean

  /**
   * Determines if the breakpoint with the specified arguments is pending.
   *
   * @param fileName The name of the file where the breakpoint resides
   * @param lineNumber The number of the line where the breakpoint resides
   * @param extraArguments The additional arguments provided to the specific
   *                       breakpoint request
   * @return True if there is at least one breakpoint at the specified location
   *         and with the provided extra arguments that is pending,
   *         otherwise false
   */
  def isBreakpointRequestWithArgsPending(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Boolean

  /**
   * Removes all breakpoint requests placed on the specified line and file.
   *
   * @param fileName The name of the file where the breakpoints reside
   * @param lineNumber The number of the line where the breakpoints reside
   * @return The collection of information about removed breakpoint requests
   */
  def removeBreakpointRequests(
    fileName: String,
    lineNumber: Int
  ): Seq[BreakpointRequestInfo]

  /**
   * Removes all breakpoint requests placed on the specified line and file.
   *
   * @param fileName The name of the file where the breakpoints reside
   * @param lineNumber The number of the line where the breakpoints reside
   * @return Success containing the collection of information about removed
   *         breakpoint requests, otherwise a failure
   */
  def tryRemoveBreakpointRequests(
    fileName: String,
    lineNumber: Int
  ): Try[Seq[BreakpointRequestInfo]] = Try(removeBreakpointRequests(
    fileName,
    lineNumber
  ))

  /**
   * Removes all breakpoint requests placed on the specified line and file with
   * the specified extra arguments.
   *
   * @param fileName The name of the file where the breakpoints reside
   * @param lineNumber The number of the line where the breakpoints reside
   * @param extraArguments the additional arguments provided to the specific
   *                       breakpoint request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  def removeBreakpointRequestWithArgs(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Option[BreakpointRequestInfo]

  /**
   * Removes all breakpoint requests placed on the specified line and file with
   * the specified extra arguments.
   *
   * @param fileName The name of the file where the breakpoints reside
   * @param lineNumber The number of the line where the breakpoints reside
   * @param extraArguments the additional arguments provided to the specific
   *                       breakpoint request
   * @return Success containing Some information if it existed (or None if it
   *         did not), otherwise a failure
   */
  def tryRemoveBreakpointRequestWithArgs(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[Option[BreakpointRequestInfo]] = Try(removeBreakpointRequestWithArgs(
    fileName,
    lineNumber,
    extraArguments: _*
  ))

  /**
   * Removes all breakpoint requests.
   *
   * @return The collection of information about removed breakpoint requests
   */
  def removeAllBreakpointRequests(): Seq[BreakpointRequestInfo]

  /**
   * Removes all breakpoint requests.
   *
   * @return Success containing the collection of information about removed
   *         breakpoint requests, otherwise a failure
   */
  def tryRemoveAllBreakpointRequests(): Try[Seq[BreakpointRequestInfo]] = Try(
    removeAllBreakpointRequests()
  )
}
