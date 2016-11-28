package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi._
import com.sun.jdi.event.MonitorWaitedEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.events.MonitorWaitedEventInfoProfile
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, ObjectInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a monitor waited event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param monitorWaitedEvent The monitor waited event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 * @param _monitor The object reference to the monitor being waited up
 * @param _monitorReferenceType The reference type of the monitor
 * @param _virtualMachine The low-level virtual machine where the event
 *                        originated
 * @param _thread The thread where the event originated
 * @param _threadReferenceType The reference type of the thread where the
 *                             event originated
 * @param _location The location of the event occurrence
 */
class PureMonitorWaitedEventInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val monitorWaitedEvent: MonitorWaitedEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
)(
  _monitor: => ObjectReference,
  _monitorReferenceType: => ReferenceType,
  _virtualMachine: => VirtualMachine,
  _thread: => ThreadReference,
  _threadReferenceType: => ReferenceType,
  _location: => Location
) extends PureLocatableEventInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  locatableEvent = monitorWaitedEvent,
  jdiArguments = jdiArguments
)(
  _virtualMachine = _virtualMachine,
  _thread = _thread,
  _threadReferenceType = _threadReferenceType,
  _location = _location
) with MonitorWaitedEventInfoProfile {
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
  override def toJavaInfo: MonitorWaitedEventInfoProfile = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newMonitorWaitedEventInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      monitorWaitedEvent = monitorWaitedEvent
    )(
      monitor = _monitor,
      monitorReferenceType = _monitorReferenceType,
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
  override def toJdiInstance: MonitorWaitedEvent =
    monitorWaitedEvent

  /**
   * Returns the monitor that was enter.
   *
   * @return The information profile about the monitor object
   */
  override def monitor: ObjectInfoProfile = infoProducer.newObjectInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    objectReference =_monitor
  )(
    virtualMachine = _virtualMachine,
    referenceType = _monitorReferenceType
  )

  /**
   * Returns the thread associated with this event.
   *
   * @return The information profile about the thread
   */
  override def thread: ThreadInfoProfile = infoProducer.newThreadInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    threadReference = _thread
  )(
    virtualMachine = _virtualMachine,
    referenceType = _threadReferenceType
  )

  /**
   * Returns whether or not the wait has timed out or been interrupted.
   *
   * @return True if timed out or interrupted, otherwise false
   */
  override def timedout: Boolean = monitorWaitedEvent.timedout()
}
