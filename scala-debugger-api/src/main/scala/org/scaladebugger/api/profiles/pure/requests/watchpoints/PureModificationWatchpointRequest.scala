package org.scaladebugger.api.profiles.pure.requests.watchpoints

import com.sun.jdi.event.ModificationWatchpointEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.lowlevel.events.EventType.ModificationWatchpointEventType
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument
import org.scaladebugger.api.lowlevel.utils.JDIArgumentGroup
import org.scaladebugger.api.lowlevel.watchpoints._
import org.scaladebugger.api.pipelines.Pipeline.IdentityPipeline
import org.scaladebugger.api.profiles.RequestHelper
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.ModificationWatchpointEventInfo
import org.scaladebugger.api.profiles.traits.requests.watchpoints.ModificationWatchpointRequest
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for modification watchpoints that adds no
 * extra logic on top of the standard JDI.
 */
trait PureModificationWatchpointRequest extends ModificationWatchpointRequest {
  protected val modificationWatchpointManager: ModificationWatchpointManager
  protected val eventManager: EventManager

  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  private lazy val eventProducer = infoProducer.eventProducer

  /** Represents helper utility to create/manage requests. */
  private lazy val requestHelper = newModificationWatchpointRequestHelper()

  /**
   * Constructs a new request helper for modification watchpoint.
   *
   * @return The new request helper
   */
  protected def newModificationWatchpointRequestHelper() = {
    // Define types for request helper
    // E: Event Type
    // EI: Event Info Type
    // RequestArgs: (Class Name, Field Name, JDI Request Args)
    // CounterKey: (Class Name, Field Name, JDI Request Args)
    type E = ModificationWatchpointEvent
    type EI = ModificationWatchpointEventInfo
    type RequestArgs = (String, String, Seq[JDIRequestArgument])
    type CounterKey = (String, String, Seq[JDIRequestArgument])

    new RequestHelper[E, EI, RequestArgs, CounterKey](
      scalaVirtualMachine = scalaVirtualMachine,
      eventManager = eventManager,
      etInstance = ModificationWatchpointEventType,
      _newRequestId = () => java.util.UUID.randomUUID().toString,
      _newRequest = (requestId, requestArgs, jdiRequestArgs) => {
        val (className, fieldName, _) = requestArgs
        modificationWatchpointManager.createModificationWatchpointRequestWithId(
          requestId,
          className,
          fieldName,
          jdiRequestArgs: _*
        )
      },
      _hasRequest = (requestArgs) => {
        val (className, fieldName, _) = requestArgs
        modificationWatchpointManager.hasModificationWatchpointRequest(className, fieldName)
      },
      _removeRequestById = (requestId) => {
        modificationWatchpointManager.removeModificationWatchpointRequestWithId(requestId)
      },
      _newEventInfo = (s, event, jdiArgs) => {
        eventProducer.newDefaultModificationWatchpointEventInfo(s, event, jdiArgs: _*)
      },
      _retrieveRequestInfo = modificationWatchpointManager.getModificationWatchpointRequestInfoWithId
    )
  }

  /**
   * Retrieves the collection of active and pending modification watchpoint requests.
   *
   * @return The collection of information on modification watchpoint requests
   */
  override def modificationWatchpointRequests: Seq[ModificationWatchpointRequestInfo] = {
    modificationWatchpointManager.modificationWatchpointRequestList ++ (modificationWatchpointManager match {
      case p: PendingModificationWatchpointSupportLike  => p.pendingModificationWatchpointRequests
      case _                                            => Nil
    })
  }

  /**
   * Constructs a stream of modification watchpoint events for field in the
   * specified class.
   *
   * @param className The full name of the class whose field to watch
   * @param fieldName The name of the field to watch
   * @param extraArguments The additional JDI arguments to provide
   * @return The stream of modification watchpoint events and any retrieved data
   *         based on requests from extra arguments
   */
  override def tryGetOrCreateModificationWatchpointRequestWithData(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Try[IdentityPipeline[ModificationWatchpointEventAndData]] = {
    val JDIArgumentGroup(rArgs, eArgs, _) = JDIArgumentGroup(extraArguments: _*)

    val requestArgs = (className, fieldName, rArgs)
    requestHelper.newRequest(requestArgs, rArgs)
      .flatMap(id => requestHelper.newEventPipeline(id, eArgs, requestArgs))
  }

  /**
   * Determines if there is any modification watchpoint request for the
   * specified class field that is pending.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return True if there is at least one modification watchpoint request with
   *         the specified field name in the specified class that is pending,
   *         otherwise false
   */
  override def isModificationWatchpointRequestPending(
    className: String,
    fieldName: String
  ): Boolean = {
    modificationWatchpointRequests.filter(m =>
      m.className == className &&
      m.fieldName == fieldName
    ).exists(_.isPending)
  }

  /**
   * Determines if there is any modification watchpoint request for the
   * specified class field with matching arguments that is pending.
   *
   * @param className      The full name of the class/object/trait containing
   *                       the field being watched
   * @param fieldName      The name of the field being watched
   * @param extraArguments The additional arguments provided to the specific
   *                       modification watchpoint request
   * @return True if there is at least one modification watchpoint request with
   *         the specified field name and arguments in the specified class that
   *         is pending, otherwise false
   */
  override def isModificationWatchpointRequestWithArgsPending(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Boolean = {
    modificationWatchpointRequests.filter(m =>
      m.className == className &&
      m.fieldName == fieldName &&
      m.extraArguments == extraArguments
    ).exists(_.isPending)
  }

  /**
   * Removes all modification watchpoint requests for the specified class field.
   *
   * @param className The full name of the class/object/trait containing the
   *                  field being watched
   * @param fieldName The name of the field being watched
   * @return The collection of information about removed modification watchpoint requests
   */
  override def removeModificationWatchpointRequests(
    className: String,
    fieldName: String
  ): Seq[ModificationWatchpointRequestInfo] = {
    modificationWatchpointRequests.filter(a =>
      a.className == className &&
        a.fieldName == fieldName
    ).filter(a =>
      modificationWatchpointManager.removeModificationWatchpointRequestWithId(a.requestId)
    )
  }

  /**
   * Removes all modification watchpoint requests for the specified class field with
   * the specified extra arguments.
   *
   * @param className      The full name of the class/object/trait containing the
   *                       field being watched
   * @param fieldName      The name of the field being watched
   * @param extraArguments the additional arguments provided to the specific
   *                       modification watchpoint request
   * @return Some information about the removed request if it existed,
   *         otherwise None
   */
  override def removeModificationWatchpointRequestWithArgs(
    className: String,
    fieldName: String,
    extraArguments: JDIArgument*
  ): Option[ModificationWatchpointRequestInfo] = {
    modificationWatchpointRequests.find(a =>
      a.className == className &&
        a.fieldName == fieldName &&
        a.extraArguments == extraArguments
    ).filter(a =>
      modificationWatchpointManager.removeModificationWatchpointRequestWithId(a.requestId)
    )
  }

  /**
   * Removes all modification watchpoint requests.
   *
   * @return The collection of information about removed modification watchpoint requests
   */
  override def removeAllModificationWatchpointRequests(): Seq[ModificationWatchpointRequestInfo] = {
    modificationWatchpointRequests.filter(a =>
      modificationWatchpointManager.removeModificationWatchpointRequestWithId(a.requestId)
    )
  }
}
