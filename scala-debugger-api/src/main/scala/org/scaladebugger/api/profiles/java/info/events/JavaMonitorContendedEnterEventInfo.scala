package org.scaladebugger.api.profiles.java.info.events

import com.sun.jdi._
import com.sun.jdi.event.MonitorContendedEnterEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.profiles.traits.info.events.{MonitorContendedEnterEventInfo, MonitorEvent}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a java implementation of a monitor contended enter event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param monitorContendedEnterEvent The monitor contended enter event to
 *                                   wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 * @param _monitor The object reference to the monitor being enter
 * @param _monitorReferenceType The reference type of the monitor
 * @param _virtualMachine The low-level virtual machine where the event
 *                        originated
 * @param _thread The thread where the event originated
 * @param _threadReferenceType The reference type of the thread where the
 *                             event originated
 * @param _location The location of the event occurrence
 */
class JavaMonitorContendedEnterEventInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val monitorContendedEnterEvent: MonitorContendedEnterEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
)(
  _monitor: => ObjectReference,
  _monitorReferenceType: => ReferenceType,
  _virtualMachine: => VirtualMachine,
  _thread: => ThreadReference,
  _threadReferenceType: => ReferenceType,
  _location: => Location
) extends JavaMonitorEventInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  monitorEvent = new MonitorEvent(monitorContendedEnterEvent),
  jdiArguments = jdiArguments
)(
  _monitor = _monitor,
  _monitorReferenceType = _monitorReferenceType,
  _virtualMachine = _virtualMachine,
  _thread = _thread,
  _threadReferenceType = _threadReferenceType,
  _location = _location
) with MonitorContendedEnterEventInfo {
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
  override def toJavaInfo: MonitorContendedEnterEventInfo = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newMonitorContendedEnterEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      monitorContendedEnterEvent = monitorContendedEnterEvent,
      jdiArguments = jdiArguments
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
  override def toJdiInstance: MonitorContendedEnterEvent =
    monitorContendedEnterEvent
}
