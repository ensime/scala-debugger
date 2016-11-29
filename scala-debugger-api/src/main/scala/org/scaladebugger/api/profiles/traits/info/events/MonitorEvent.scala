package org.scaladebugger.api.profiles.traits.info.events

import com.sun.jdi.event._
import com.sun.jdi.request.EventRequest
import com.sun.jdi.{Location, ObjectReference, ThreadReference, VirtualMachine}

/**
 * Represents an abstraction on top of the different monitor events.
 *
 * @param locatableEvent The underlying locatable event that represents
 *                       a monitor event
 */
sealed class MonitorEvent(
  val locatableEvent: LocatableEvent
) extends LocatableEvent {
  require(
    locatableEvent.isInstanceOf[MonitorContendedEnteredEvent] ||
    locatableEvent.isInstanceOf[MonitorContendedEnterEvent] ||
    locatableEvent.isInstanceOf[MonitorWaitedEvent] ||
    locatableEvent.isInstanceOf[MonitorWaitEvent],
    s"${locatableEvent.getClass.getName} is not a monitor event!"
  )

  /**
   * Returns the monitor object reference.
   *
   * @return The JDI object reference of the monitor
   */
  def monitor(): ObjectReference = locatableEvent match {
    case e: MonitorContendedEnteredEvent  => e.monitor()
    case e: MonitorContendedEnterEvent    => e.monitor()
    case e: MonitorWaitedEvent            => e.monitor()
    case e: MonitorWaitEvent              => e.monitor()
  }

  /**
   * Returns the thread in which this event occurred or the thread
   * in which the monitor wait/waited event occurred.
   *
   * @return The JDI thread reference
   */
  override def thread(): ThreadReference = locatableEvent match {
    case e: MonitorContendedEnteredEvent  => e.thread()
    case e: MonitorContendedEnterEvent    => e.thread()
    case e: MonitorWaitedEvent            => e.thread()
    case e: MonitorWaitEvent              => e.thread()
  }

  override def virtualMachine(): VirtualMachine = locatableEvent.virtualMachine()
  override def request(): EventRequest = locatableEvent.request()
  override def location(): Location = locatableEvent.location()
}

