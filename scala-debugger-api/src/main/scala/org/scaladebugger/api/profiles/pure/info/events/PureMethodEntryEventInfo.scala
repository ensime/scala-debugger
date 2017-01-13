package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event.MethodEntryEvent
import com.sun.jdi._
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, MethodInfo}
import org.scaladebugger.api.profiles.traits.info.events.MethodEntryEventInfo
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a method entry event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param methodEntryEvent The method entry event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 * @param _method The method being entered
 * @param _virtualMachine The low-level virtual machine where the event
 *                        originated
 * @param _thread The thread where the event originated
 * @param _threadReferenceType The reference type of the thread where the
 *                             event originated
 * @param _location The location of the event occurrence
 */
class PureMethodEntryEventInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val methodEntryEvent: MethodEntryEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
)(
  _method: => Method,
  _virtualMachine: => VirtualMachine,
  _thread: => ThreadReference,
  _threadReferenceType: => ReferenceType,
  _location: => Location
) extends PureLocatableEventInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  locatableEvent = methodEntryEvent,
  jdiArguments = jdiArguments
)(
  _virtualMachine = _virtualMachine,
  _thread = _thread,
  _threadReferenceType = _threadReferenceType,
  _location = _location
) with MethodEntryEventInfo {
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
  override def toJavaInfo: MethodEntryEventInfo = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newMethodEntryEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      methodEntryEvent = methodEntryEvent,
      jdiArguments = jdiArguments
    )(
      method = _method,
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
  override def toJdiInstance: MethodEntryEvent = methodEntryEvent

  /**
   * Returns the method that was entered.
   *
   * @return The information profile about the method
   */
  override def method: MethodInfo = infoProducer.newMethodInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    method = _method
  )
}
