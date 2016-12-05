package org.scaladebugger.api.lowlevel.breakpoints

import com.sun.jdi.request.{EventRequestManager, BreakpointRequest}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.{EnabledProperty, SuspendPolicyProperty}
import org.scaladebugger.api.utils.{MultiMap, Logging}
import com.sun.jdi.Location

import scala.collection.JavaConverters._
import scala.util.{Try, Failure}

/**
 * Represents the manager for breakpoint requests.
 *
 * @param eventRequestManager The manager used to create breakpoint requests
 * @param classManager The class manager associated with the virtual machine,
 *                      used to retrieve location information
 */
class StandardBreakpointManager(
  private val eventRequestManager: EventRequestManager,
  private val classManager: ClassManager
) extends BreakpointManager with Logging {
  import org.scaladebugger.api.lowlevel.requests.Implicits._

  private val breakpointRequests =
    new MultiMap[BreakpointRequestInfo, Seq[BreakpointRequest]]

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints in the form of information
   */
  override def breakpointRequestList: Seq[BreakpointRequestInfo] =
    breakpointRequests.keys

  /**
   * Retrieves the list of breakpoints contained by this manager.
   *
   * @return The collection of breakpoints by id
   */
  override def breakpointRequestListById: Seq[String] = breakpointRequests.ids

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
  override def createBreakpointRequestWithId(
    requestId: String,
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIRequestArgument*
  ): Try[String] = {
    // Retrieve the available locations for the specified line
    val locations = classManager
      .linesAndLocationsForFile(fileName)
      .flatMap(_.get(lineNumber))
      .getOrElse(Nil)

    // Exit early if no locations are available
    if (locations.isEmpty)
      return Failure(NoBreakpointLocationFound(fileName, lineNumber))

    val arguments = Seq(
      SuspendPolicyProperty.EventThread,
      EnabledProperty(value = true)
    ) ++ extraArguments

    // TODO: Back out breakpoint creation if a failure occurs
    val requests = Try(locations.map(
      eventRequestManager.createBreakpointRequest(_: Location, arguments: _*)
    ))

    val isPending = false
    if (requests.isSuccess) {
      val l = s"$fileName:$lineNumber"
      logger.trace(s"Created breakpoint request with id '$requestId' at $l")
      breakpointRequests.putWithId(
        requestId,
        BreakpointRequestInfo(
          requestId,
          isPending,
          fileName,
          lineNumber,
          extraArguments
        ),
        requests.get
      )
    }

    // If no exception was thrown, assume that we succeeded
    requests.map(_ => requestId)
  }

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
  ): Boolean = {
    breakpointRequests.hasWithKeyPredicate(b =>
      b.fileName == fileName && b.lineNumber == lineNumber
    )
  }

  /**
   * Determines whether or not the breakpoint with the specified id exists.
   *
   * @param requestId The id of the request
   *
   * @return True if a breakpoint exists, otherwise false
   */
  override def hasBreakpointRequestWithId(requestId: String): Boolean = {
    breakpointRequests.hasWithId(requestId)
  }

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
  ): Option[Seq[BreakpointRequest]] = {
    val requests = breakpointRequests.getWithKeyPredicate(b =>
      b.fileName == fileName && b.lineNumber == lineNumber
    ).flatten

    if (requests.nonEmpty) Some(requests) else None
  }

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
  ): Option[Seq[BreakpointRequest]] = {
    breakpointRequests.getWithId(requestId)
  }

  /**
   * Returns the arguments for a breakpoint request with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return Some breakpoint arguments if found, otherwise None
   */
  override def getBreakpointRequestInfoWithId(
    requestId: String
  ): Option[BreakpointRequestInfo] = {
    breakpointRequests.getKeyWithId(requestId)
  }

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
  ): Boolean = {
    val ids = breakpointRequests.getIdsWithKeyPredicate(b =>
      b.fileName == fileName && b.lineNumber == lineNumber
    )

    ids.nonEmpty && ids.forall(removeBreakpointRequestWithId)
  }

  /**
   * Removes the breakpoint with the specified id.
   *
   * @param requestId The id of the request
   *
   * @return True if successfully removed breakpoint, otherwise false
   */
  override def removeBreakpointRequestWithId(
    requestId: String
  ): Boolean = {
    val requests = breakpointRequests.removeWithId(requestId)

    requests.map(_.asJava).foreach(eventRequestManager.deleteEventRequests)

    requests.nonEmpty
  }
}
