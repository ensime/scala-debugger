package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.{Location, ReferenceType, ThreadReference, VirtualMachine}
import com.sun.jdi.event.LocatableEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.events.LocatableEventInfoProfile
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, LocationInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a locatable event info profile that
 * adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param locatableEvent The locatable event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 * @param _virtualMachine The low-level virtual machine where the event
 *                        originated
 * @param _thread The thread where the event originated
 * @param _threadReferenceType The reference type of the thread where the
 *                             event originated
 * @param _location The location of the event occurrence
 */
class PureLocatableEventInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val locatableEvent: LocatableEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
)(
  _virtualMachine: => VirtualMachine,
  _thread: => ThreadReference,
  _threadReferenceType: => ReferenceType,
  _location: => Location
) extends PureEventInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  event = locatableEvent,
  jdiArguments = jdiArguments
) with LocatableEventInfoProfile {
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
  override def toJavaInfo: LocatableEventInfoProfile = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newLocatableEventInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      locatableEvent = locatableEvent,
      jdiArguments = jdiArguments
    )(
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
  override def toJdiInstance: LocatableEvent = locatableEvent

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
   * Returns the location associated with this event.
   *
   * @return The information profile about the location
   */
  override def location: LocationInfoProfile = infoProducer.newLocationInfoProfile(
    scalaVirtualMachine = scalaVirtualMachine,
    location = _location
  )
}
