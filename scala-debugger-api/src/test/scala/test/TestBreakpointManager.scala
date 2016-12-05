package test

import com.sun.jdi.request.BreakpointRequest
import org.scaladebugger.api.lowlevel.breakpoints.{BreakpointRequestInfo, BreakpointManager}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

import scala.util.Try

/**
 * Test breakpoint manager that merely invokes the provided breakpoint manager
 * underneath to make it easier to mock.
 *
 * @param breakpointManager The underlying breakpoint manager used to execute
 *                          all methods
 */
class TestBreakpointManager(
  private val breakpointManager: BreakpointManager
) extends BreakpointManager {
  override def breakpointRequestList: Seq[BreakpointRequestInfo] =
    breakpointManager.breakpointRequestList
  override def removeBreakpointRequestWithId(requestId: String): Boolean =
    breakpointManager.removeBreakpointRequestWithId(requestId)
  override def removeBreakpointRequest(fileName: String, lineNumber: Int): Boolean =
    breakpointManager.removeBreakpointRequest(fileName, lineNumber)
  override def getBreakpointRequestWithId(requestId: String): Option[Seq[BreakpointRequest]] =
    breakpointManager.getBreakpointRequestWithId(requestId)
  override def breakpointRequestListById: Seq[String] =
    breakpointManager.breakpointRequestListById
  override def hasBreakpointRequest(fileName: String, lineNumber: Int): Boolean =
    breakpointManager.hasBreakpointRequest(fileName, lineNumber)
  override def createBreakpointRequestWithId(requestId: String, fileName: String, lineNumber: Int, extraArguments: JDIRequestArgument*): Try[String] =
    breakpointManager.createBreakpointRequestWithId(requestId, fileName, lineNumber, extraArguments: _*)
  override def getBreakpointRequest(fileName: String, lineNumber: Int): Option[Seq[BreakpointRequest]] =
    breakpointManager.getBreakpointRequest(fileName, lineNumber)
  override def getBreakpointRequestInfoWithId(requestId: String): Option[BreakpointRequestInfo] =
    breakpointManager.getBreakpointRequestInfoWithId(requestId)
  override def hasBreakpointRequestWithId(requestId: String): Boolean =
    breakpointManager.hasBreakpointRequestWithId(requestId)
}
