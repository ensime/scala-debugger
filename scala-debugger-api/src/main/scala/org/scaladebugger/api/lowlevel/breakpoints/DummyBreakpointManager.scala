package org.scaladebugger.api.lowlevel.breakpoints

import com.sun.jdi.request.BreakpointRequest
import org.scaladebugger.api.lowlevel.DummyOperationException
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.{Failure, Try}

/**
 * Represents a breakpoint manager whose operations do nothing.
 */
class DummyBreakpointManager extends BreakpointManager {
  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints in the form of information
   */
  override def breakpointRequestList: Seq[BreakpointRequestInfo] = Nil

  /**
   * Removes the breakpoint with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  override def removeBreakpointRequestWithId(requestId: String): Boolean = false

  /**
   * Removes the breakpoint on the specified line of the file.
   *
   * @param fileName The name of the file to remove the breakpoint
   * @param lineNumber The number of the line to break
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  override def removeBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Boolean = false

  /**
   * Returns the collection of breakpoints with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some collection of breakpoints for the specified line, or None if
   *         the specified line has no breakpoints
   */
  override def getBreakpointRequestWithId(
    requestId: String
  ): Option[Seq[BreakpointRequest]] = None

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints by id
   */
  override def breakpointRequestListById: Seq[String] = Nil

  /**
   * Determines whether or not the breakpoint for the specific file's line.
   *
   * @param fileName The name of the file whose line to reference
   * @param lineNumber The number of the line to check for a breakpoint
   *
   * @return True if a breakpoint exists, otherwise false
   */
  override def hasBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Boolean = false

  /**
   * Creates and enables a breakpoint on the specified line of the class.
   *
   * @param requestId The id of the request used for lookup and removal
   * @param fileName The name of the file to set a breakpoint
   * @param lineNumber The number of the line to break
   * @param extraArguments Any additional arguments to provide to the request
   *
   * @return Success(id) if successful or pending, otherwise Failure
   */
  override def createBreakpointRequestWithId(
    requestId: String,
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = Failure(new DummyOperationException)

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
  override def getBreakpointRequest(
    fileName: String,
    lineNumber: Int
  ): Option[Seq[BreakpointRequest]] = None

  /**
   * Returns the information for a breakpoint request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some breakpoint information if found, otherwise None
   */
  override def getBreakpointRequestInfoWithId(
    requestId: String
  ): Option[BreakpointRequestInfo] = None

  /**
   * Determines whether or not the breakpoint with the specified id exists.
   *
   * @param requestId The id of the request
   *
   * @return True if a breakpoint exists, otherwise false
   */
  override def hasBreakpointRequestWithId(requestId: String): Boolean = false
}
