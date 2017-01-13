package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event.ExceptionEvent
import com.sun.jdi._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, LocationInfo, ObjectInfo}
import org.scaladebugger.api.profiles.traits.info.events.ExceptionEventInfo
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a exception event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param exceptionEvent The exception event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 * @param _catchLocation Some location where the event will be caught if a
 *                       caught exception, otherwise None if uncaught
 * @param _exception The reference to the exception object thrown
 * @param _exceptionReferenceType The reference type of the thrown exception
 * @param _virtualMachine The low-level virtual machine where the event
 *                        originated
 * @param _thread The thread where the event originated
 * @param _threadReferenceType The reference type of the thread where the
 *                             event originated
 * @param _location The location of the event occurrence
 */
class PureExceptionEventInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val exceptionEvent: ExceptionEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
)(
  _catchLocation: => Option[Location],
  _exception: => ObjectReference,
  _exceptionReferenceType: => ReferenceType,
  _virtualMachine: => VirtualMachine,
  _thread: => ThreadReference,
  _threadReferenceType: => ReferenceType,
  _location: => Location
) extends PureLocatableEventInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  locatableEvent = exceptionEvent,
  jdiArguments = jdiArguments
)(
  _virtualMachine = _virtualMachine,
  _thread = _thread,
  _threadReferenceType = _threadReferenceType,
  _location = _location
) with ExceptionEventInfo {
  /**
   * Returns whether or not this info profile represents the low-level Java
   * implementation.
   *
   * @return If true, this profile represents the low-level Java information,
   *         otherwise this profile represents something higher-level like
   *         Scala, Jython, or JRuby
   */
  override def isJavaInfo: Boolean = true

  /**
   * Converts the current profile instance to a representation of
   * low-level Java instead of a higher-level abstraction.
   *
   * @return The profile instance providing an implementation corresponding
   *         to Java
   */
  override def toJavaInfo: ExceptionEventInfo = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newExceptionEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      exceptionEvent = exceptionEvent,
      jdiArguments = jdiArguments
    )(
      catchLocation = _catchLocation,
      exception = _exception,
      exceptionReferenceType = _exceptionReferenceType,
      virtualMachine = _virtualMachine,
      thread = _thread,
      threadReferenceType = _threadReferenceType,
      location = _location
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ExceptionEvent = exceptionEvent

  /**
   * Returns the location where the exception will be caught.
   *
   * @return Some information profile about the location if the exception is
   *         a caught exception, otherwise None if it is uncaught
   */
  override def catchLocation: Option[LocationInfo] = {
    _catchLocation.map(l => infoProducer.newLocationInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      location = l
    ))
  }

  /**
   * Returns the thrown exception object.
   *
   * @return The information profile about the exception object
   */
  override def exception: ObjectInfo = {
    infoProducer.newObjectInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      objectReference = _exception
    )(
      virtualMachine = _virtualMachine,
      referenceType = _exceptionReferenceType
    )
  }
}
