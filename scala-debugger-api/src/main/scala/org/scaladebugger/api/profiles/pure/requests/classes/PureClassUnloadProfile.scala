package org.scaladebugger.api.profiles.pure.requests.classes

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

import com.sun.jdi.event.ClassUnloadEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes._
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
import org.scaladebugger.api.profiles.traits.requests.classes.ClassUnloadProfile
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.ClassUnloadEventInfoProfile
import org.scaladebugger.api.utils.{Memoization, MultiMap}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a pure profile for class unload events that adds no
 * extra logic on top of the standard JDI.
 */
trait PureClassUnloadProfile extends ClassUnloadProfile {
  protected val classUnloadManager: ClassUnloadManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newClassUnloadRequestHelper()

  /**
   * Constructs a new request helper for class unload.
   *
   * @return The new request helper
   */
  protected def newClassUnloadRequestHelper() = {
    type E = ClassUnloadEvent
    type EI = ClassUnloadEventInfoProfile
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = ClassUnloadEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        classUnloadManager.createClassUnloadRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        classUnloadManager.classUnloadRequestList
          .flatMap(classUnloadManager.getClassUnloadRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        classUnloadManager.removeClassUnloadRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultClassUnloadEventInfoProfile(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = classUnloadManager.getClassUnloadRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending class unload requests.
   *
   * @return The collection of information on class unload requests
   */
  override def classUnloadRequests: Seq[ClassUnloadRequestInfo] = {
    val activeRequests = classUnloadManager.classUnloadRequestList.flatMap(
      classUnloadManager.getClassUnloadRequestInfo
    )

    activeRequests ++ (classUnloadManager match {
      case p: PendingClassUnloadSupportLike => p.pendingClassUnloadRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of class unload events.
   *
   * @param extraArguments The additional JDI arguments to provide
   *
   * @return The stream of class unload events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateClassUnloadRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassUnloadEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the class unload request with the specified arguments
   * is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       class unload request
   * @return True if there is at least one class unload request with the
   *         provided extra arguments that is pending, otherwise false
   */
  override def isClassUnloadRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    classUnloadRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all class unload requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       class unload request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeClassUnloadRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ClassUnloadRequestInfo] = {
    classUnloadRequests.find(_.extraArguments == extraArguments).filter(c =>
      classUnloadManager.removeClassUnloadRequest(c.requestId)
    )
  }

  /**
   * Removes all class unload requests.
   *
   * @return The collection of information about removed class unload requests
   */
  override def removeAllClassUnloadRequests(): Seq[ClassUnloadRequestInfo] = {
    classUnloadRequests.filter(c =>
      classUnloadManager.removeClassUnloadRequest(c.requestId)
    )
  }
}

