package org.scaladebugger.api.lowlevel.breakpoints

import com.sun.jdi.request.BreakpointRequest
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Represents the manager for breakpoint requests.
 */
trait BreakpointManager {
  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints in the form of information
   */
  def breakpointRequestList: Seq[BreakpointRequestInfo]

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints by id
   */
  def breakpointRequestListById: Seq[String]

  /**
   * Creates and enables a breakpoint on the specified line of the class.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createBreakpointRequestWithId(
    requestId: String,
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String]


  /**
   * Creates and enables a breakpoint on the specified line of the class.
   *
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createBreakpointRequest(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = createBreakpointRequestWithId(
    newRequestId(),
    fileName,
    lineNumber,
    extraArguments: _*
  )

  /**
   * Creates and enables a breakpoint based on the specified information.
   *
   * @param breakpointRequestInfo The information used to create the breakpoint
   *                              request
   *
   * @return Success(id) if successful, otherwise Failure
   */
  def createBreakpointRequestFromInfo(
    breakpointRequestInfo: BreakpointRequestInfo
  ): Try[String] = createBreakpointRequestWithId(
    breakpointRequestInfo.requestId,
    breakpointRequestInfo.fileName,
    breakpointRequestInfo.lineNumber,
    breakpointRequestInfo.extraArguments: _*
  )

  /**
   * Determines whether or not the breakpoint for the specific file's line.
   *
   * @param fileName The name of the file whose line to reference
   * @param lineNumber The number of the line to check for a breakpoint
   *
   * @return True if a breakpoint exists, otherwise false
   */
  def hasBreakpointRequest(fileName: String, lineNumber: Int): Boolean

  /**
   * Determines whether or not the breakpoint with the specified id exists.
   *
   * @param requestId The id of the request
   *
   * @return True if a breakpoint exists, otherwise false
   */
  def hasBreakpointRequestWithId(requestId: String): Boolean

  /**
   * Returns the collection of breakpoints representing the breakpoint for the
   * specified line.
   *
   * @param fileName The name of the file whose line to reference
   * @param lineNumber The number of the line to check for breakpoints
   *
   * @return Some collection of breakpoints for the specified line, or None if
   *         the specified line has no breakpoints
   */
  def getBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Option[Seq[BreakpointRequest]]

  /**
   * Returns the collection of breakpoints with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some collection of breakpoints for the specified line, or None if
   *         the specified line has no breakpoints
   */
  def getBreakpointRequestWithId(
    requestId: String
  ): Option[Seq[BreakpointRequest]]

  /**
   * Returns the information for a breakpoint request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some breakpoint information if found, otherwise None
   */
  def getBreakpointRequestInfoWithId(
    requestId: String
  ): Option[BreakpointRequestInfo]

  /**
   * Removes the breakpoint on the specified line of the file.
   *
   * @param fileName The name of the file to remove the breakpoint
   * @param lineNumber The number of the line to break
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  def removeBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Boolean

  /**
   * Removes the breakpoint with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  def removeBreakpointRequestWithId(
    requestId: String
  ): Boolean

  /**
   * Generates an id for a new request.
   *
   * @return The id as a string
   */
  protected def newRequestId(): String = java.util.UUID.randomUUID().toString
}
