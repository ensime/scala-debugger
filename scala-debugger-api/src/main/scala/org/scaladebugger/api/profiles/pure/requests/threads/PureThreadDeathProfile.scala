package org.scaladebugger.api.profiles.pure.requests.threads
//import acyclic.file

import com.sun.jdi.event.ThreadDeathEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.ThreadDeathEventType
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.threads._
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.ThreadDeathEventInfoProfile
import org.scaladebugger.api.profiles.traits.requests.threads.ThreadDeathProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for thread start events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureThreadDeathProfile extends ThreadDeathProfile {
  protected val threadDeathManager: ThreadDeathManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newThreadDeathRequestHelper()

  /**
   * Constructs a new request helper for access watchpoint.
   *
   * @return The new request helper
   */
  protected def newThreadDeathRequestHelper() = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (Class Name, Field Name, JDI Request Args)
    // CounterKey: (Class Name, Field Name, JDI Request Args)
    type E = ThreadDeathEvent
    type EI = ThreadDeathEventInfoProfile
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = ThreadDeathEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        threadDeathManager.createThreadDeathRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        threadDeathManager.threadDeathRequestList
          .flatMap(threadDeathManager.getThreadDeathRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        threadDeathManager.removeThreadDeathRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultThreadDeathEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = threadDeathManager.getThreadDeathRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending thread death requests.
   *
   * @return The collection of information on thread death requests
   */
  override def threadDeathRequests: Seq[ThreadDeathRequestInfo] = {
    val activeRequests = threadDeathManager.threadDeathRequestList.flatMap(
      threadDeathManager.getThreadDeathRequestInfo
    )

    activeRequests ++ (threadDeathManager match {
      case p: PendingThreadDeathSupportLike => p.pendingThreadDeathRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of thread start events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of thread start events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateThreadDeathRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ThreadDeathEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the thread death request with the specified
   * arguments is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       thread death request
   * @return True if there is at least one thread death request
   *         with the provided extra arguments that is pending, otherwise false
   */
  override def isThreadDeathRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    threadDeathRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all thread death requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       thread death request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeThreadDeathRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ThreadDeathRequestInfo] = {
    threadDeathRequests.find(_.extraArguments == extraArguments).filter(c =>
      threadDeathManager.removeThreadDeathRequest(c.requestId)
    )
  }

  /**
   * Removes all thread death requests.
   *
   * @return The collection of information about removed thread death requests
   */
  override def removeAllThreadDeathRequests(): Seq[ThreadDeathRequestInfo] = {
    threadDeathRequests.filter(c =>
      threadDeathManager.removeThreadDeathRequest(c.requestId)
    )
  }
}

