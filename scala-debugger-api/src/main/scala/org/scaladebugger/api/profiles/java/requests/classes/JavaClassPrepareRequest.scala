package org.scaladebugger.api.profiles.java.requests.classes


import com.sun.jdi.event.ClassPrepareEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes._
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.requests.properties.UniqueIdProperty
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.ClassPrepareEventInfo
import org.scaladebugger.api.profiles.traits.requests.classes.ClassPrepareRequest
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a java profile for class prepare events that adds no
 * extra logic on top of the standard JDI.
 */
trait JavaClassPrepareRequest extends ClassPrepareRequest {
  protected val classPrepareManager: ClassPrepareManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newClassPrepareRequestHelper()

  /**
   * Constructs a new request helper for class prepare.
   *
   * @return The new request helper
   */
  protected def newClassPrepareRequestHelper() = {
    type E = ClassPrepareEvent
    type EI = ClassPrepareEventInfo
    type RequestArgs = Seq[JDIRequestArgument]
    type CounterKey = Seq[JDIRequestArgument]

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = ClassPrepareEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, _, jdiRequestArgs) => {
        classPrepareManager.createClassPrepareRequestWithId(
          requestId,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        classPrepareManager.classPrepareRequestList
          .flatMap(classPrepareManager.getClassPrepareRequestInfo)
          .map(_.extraArguments)
          .map(_.filterNot(_.isInstanceOf[UniqueIdProperty]))
          .contains(requestArgs)
      },
      _removeRequestById = (requestId) => {
        classPrepareManager.removeClassPrepareRequest(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultClassPrepareEventInfo(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = classPrepareManager.getClassPrepareRequestInfo
    )
  }

  /**
   * Retrieves the collection of active and pending class prepare requests.
   *
   * @return The collection of information on class prepare requests
   */
  override def classPrepareRequests: Seq[ClassPrepareRequestInfo] = {
    val activeRequests = classPrepareManager.classPrepareRequestList.flatMap(
      classPrepareManager.getClassPrepareRequestInfo
    )

    activeRequests ++ (classPrepareManager match {
      case p: PendingClassPrepareSupportLike => p.pendingClassPrepareRequests
      case _                                => Nil
    })
  }

  /**
   * Constructs a stream of class prepare events.
   *
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of class prepare events and any retrieved data based on
   *         requests from extra arguments
   */
  override def tryGetOrCreateClassPrepareRequestWithData(
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ClassPrepareEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = rArgs
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if the class prepare request with the specified arguments
   * is pending.
   *
   * @param extraArguments The additional arguments provided to the specific
   *                       class prepare request
   * @return True if there is at least one class prepare request with the
   *         provided extra arguments that is pending, otherwise false
   */
  override def isClassPrepareRequestWithArgsPending(
    extraArguments: JDIArgument*
  ): Boolean = {
    classPrepareRequests
      .filter(_.extraArguments == extraArguments)
      .exists(_.isPending)
  }

  /**
   * Removes all class prepare requests with the specified extra arguments.
   *
   * @param extraArguments the additional arguments provided to the specific
   *                       class prepare request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeClassPrepareRequestWithArgs(
    extraArguments: JDIArgument*
  ): Option[ClassPrepareRequestInfo] = {
    classPrepareRequests.find(_.extraArguments == extraArguments).filter(c =>
      classPrepareManager.removeClassPrepareRequest(c.requestId)
    )
  }

  /**
   * Removes all class prepare requests.
   *
   * @return The collection of information about removed class prepare requests
   */
  override def removeAllClassPrepareRequests(): Seq[ClassPrepareRequestInfo] = {
    classPrepareRequests.filter(c =>
      classPrepareManager.removeClassPrepareRequest(c.requestId)
    )
  }
}

