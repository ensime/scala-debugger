package org.scaladebugger.api.profiles.pure.requests.breakpoints
//import acyclic.file

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.breakpoints.{BreakpointManager, BreakpointRequestInfo, PendingBreakpointSupportLike}
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.events.filters.UniqueIdPropertyFilter
import org.scaladebugger.api.lowlevel.events.{EventManager, JDIEventArgument}
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.Constants._
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.requests.breakpoints.BreakpointProfile
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.BreakpointEventInfoProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for breakpoints that adds no extra logic on top
 * of the standard JDI.
 */
trait PureBreakpointProfile extends BreakpointProfile {
  protected val breakpointManager: BreakpointManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newBreakpointRequestHelper()

  /**
   * Constructs a new request helper for method exit.
   *
   * @return The new request helper
   */
  protected def newBreakpointRequestHelper() = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (File Name, Line Number, JDI Request Args)
    // CounterKey: (File Name, Line Number, JDI Request Args)
    type E = BreakpointEvent
    type EI = BreakpointEventInfoProfile
    type RequestArgs = (String, Int, Seq[JDIRequestArgument])
    type CounterKey = (String, Int, Seq[JDIRequestArgument])

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = BreakpointEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, requestArgs, jdiRequestArgs) => {
        val (fileName, lineNumber, _) = requestArgs
        breakpointManager.createBreakpointRequestWithId(
          requestId,
          fileName,
          lineNumber,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        val (fileName, lineNumber, _) = requestArgs
        breakpointManager.hasBreakpointRequest(fileName, lineNumber)
      },
      _removeRequestById = (requestId) => {
        breakpointManager.removeBreakpointRequestWithId(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultBreakpointEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = breakpointManager.getBreakpointRequestInfoWithId
    )
  }

  /**
   * Retrieves the collection of active and pending breakpoint requests.
   *
   * @return The collection of information on breakpoint requests
   */
  override def breakpointRequests: Seq[BreakpointRequestInfo] = {
    breakpointManager.breakpointRequestList ++ (breakpointManager match {
      case p: PendingBreakpointSupportLike  => p.pendingBreakpointRequests
      case _                                => Nil
    })
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
  override def tryGetOrCreateBreakpointRequestWithData(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[BreakpointEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = (fileName, lineNumber, rArgs)
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if there is any breakpoint on the specified file and line
   * that is pending.
   *
   * @param fileName   The name of the file where the breakpoint resides
   * @param lineNumber The number of the line where the breakpoint resides
   * @return True if there is at least one breakpoint at the specified location
   *         that is pending, otherwise false
   */
  override def isBreakpointRequestPending(
    fileName: String,
    lineNumber: Int
  ): Boolean = breakpointRequests.filter(b =>
    b.fileName == fileName && b.lineNumber == lineNumber
  ).exists(_.isPending)

  /**
   * Determines if the breakpoint with the specified arguments is pending.
   *
   * @param fileName       The name of the file where the breakpoint resides
   * @param lineNumber     The number of the line where the breakpoint resides
   * @param extraArguments The additional arguments provided to the specific
   *                       breakpoint request
   * @return True if there is at least one breakpoint at the specified location
   *         and with the provided extra arguments that is pending,
   *         otherwise false
   */
  override def isBreakpointRequestWithArgsPending(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Boolean = breakpointRequests.filter(b =>
    b.fileName == fileName && b.lineNumber == lineNumber &&
      b.extraArguments == extraArguments
  ).exists(_.isPending)

  /**
   * Removes all breakpoint requests placed on the specified line and file.
   *
   * @param fileName   The name of the file where the breakpoints reside
   * @param lineNumber The number of the line where the breakpoints reside
   * @return The collection of information about removed breakpoint requests
   */
  override def removeBreakpointRequests(
    fileName: String,
    lineNumber: Int
  ): Seq[BreakpointRequestInfo] = {
    removeRequestsUsingInfo(breakpointRequests.filter(b =>
      b.fileName == fileName &&
      b.lineNumber == lineNumber
    ))
  }

  /**
   * Removes all breakpoint requests placed on the specified line and file with
   * the specified extra arguments.
   *
   * @param fileName       The name of the file where the breakpoints reside
   * @param lineNumber     The number of the line where the breakpoints reside
   * @param extraArguments the additional arguments provided to the specific
   *                       breakpoint request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeBreakpointRequestWithArgs(
    fileName: String,
    lineNumber: Int,
    extraArguments: JDIArgument*
  ): Option[BreakpointRequestInfo] = {
    removeRequestsUsingInfo(breakpointRequests.filter(b =>
      b.fileName == fileName &&
      b.lineNumber == lineNumber &&
      b.extraArguments == extraArguments
    )).headOption
  }

  /**
   * Removes all breakpoint requests.
   *
   * @return The collection of information about removed breakpoint requests
   */
  override def removeAllBreakpointRequests(): Seq[BreakpointRequestInfo] = {
    removeRequestsUsingInfo(breakpointRequests)
  }

  /**
   * Removes all requests based on the provided information.
   *
   * @param requests The information about the requests to remove
   * @return The information of the removed requests
   */
  private def removeRequestsUsingInfo(
    requests: Seq[BreakpointRequestInfo]
  ): Seq[BreakpointRequestInfo] = {
    requests.filter(r =>
      breakpointManager.removeBreakpointRequestWithId(r.requestId)
    )
  }
}
