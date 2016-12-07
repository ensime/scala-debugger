package org.scaladebugger.api.profiles.pure.requests.threads


import com.sun.jdi.event.ThreadStartEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.ThreadStartEventType
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.threads._
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.ThreadStartEventInfo
import org.scaladebugger.api.profiles.traits.requests.threads.ThreadStartRequest
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for thread start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadStartRequest extends ThreadStartRequest {
  protected val threadStartManager: ThreadStartManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newThreadStartRequestHelper()

  /**
   * Constructs a new request helper for thread start.
   *
   * @return The new request helper
   */
  protected def newThreadStartRequestHelper() = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (Class Name, Field Name, JDI Request Args)
    // CounterKey: (Class Name, Field Name, JDI Request Args)
    type E = ThreadStartEvent
    type EI = ThreadStartEventInfo
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = ThreadStartEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        threadStartManager.createThreadStartRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        threadStartManager.threadStartRequestList
          .flatMap(threadStartManager.getThreadStartRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        threadStartManager.removeThreadStartRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultThreadStartEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = threadStartManager.getThreadStartRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending thread start requests.
   *
   * @return The collection of information on thread start requests
   */
  override def threadStartRequests: Seq[ThreadStartRequestInfo] = {
    val activeRequests = threadStartManager.threadStartRequestList.flatMap(
      threadStartManager.getThreadStartRequestInfo
    )

    activeRequests ++ (threadStartManager match {
      case p: PendingThreadStartSupportLike => p.pendingThreadStartRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateThreadStartRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadStartEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the thread start request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       thread start request
   * @return True if there is at least one thread start request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isThreadStartRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    threadStartRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all thread start requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       thread start request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeThreadStartRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ThreadStartRequestInfo] = {
    threadStartRequests.find(_.extraArguments == extraArguments).filter(c =>
      threadStartManager.removeThreadStartRequest(c.requestId)
    )
  }

  /**
   * Removes all thread start requests.
   *
   * @return The collection of information about removed thread start requests
   */
  override def removeAllThreadStartRequests(): Seq[ThreadStartRequestInfo] = {
    threadStartRequests.filter(c =>
      threadStartManager.removeThreadStartRequest(c.requestId)
    )
  }
}

