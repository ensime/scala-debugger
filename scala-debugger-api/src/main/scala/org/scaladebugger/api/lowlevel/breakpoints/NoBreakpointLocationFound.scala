package org.scaladebugger.api.lowlevel.breakpoints

/**
 * Represents an exception that occurred when attempting to create a breakpoint
 * request and the desired class or line was not found on the remote JVM.
 *
 * @param fileName The name of the file where the breakpoint was attempted
 * @param lineNumber The number of the line where the breakpoint was attempted
 */
case class NoBreakpointLocationFound(fileName: String, lineNumber: Int)
  extends Throwable(s"No location for $fileName:$lineNumber was found!")
