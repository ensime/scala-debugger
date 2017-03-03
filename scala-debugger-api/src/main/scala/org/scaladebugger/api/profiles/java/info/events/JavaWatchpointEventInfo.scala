package org.scaladebugger.api.profiles.java.info.events

import com.sun.jdi._
import com.sun.jdi.event.WatchpointEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.profiles.traits.info.events.WatchpointEventInfo
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a java implementation of a watchpoint event info profile that
 * adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param watchpointEvent The watchpoint event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 * @param _container Either the object or class (if static) containing the
 *                   field being accessed/modified
 * @param _field The field being accessed/modified
 * @param _virtualMachine The low-level virtual machine where the event
 *                        originated
 * @param _thread The thread where the event originated
 * @param _threadReferenceType The reference type of the thread where the
 *                             event originated
 * @param _location The location of the access/modification occurrence
 */
class JavaWatchpointEventInfo(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducer,
  private val watchpointEvent: WatchpointEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
)(
  _container: => Either[ObjectReference, ReferenceType],
  _field: => Field,
  _virtualMachine: => VirtualMachine,
  _thread: => ThreadReference,
  _threadReferenceType: => ReferenceType,
  _location: => Location
) extends JavaLocatableEventInfo(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  locatableEvent = watchpointEvent,
  jdiArguments = jdiArguments
)(
  _virtualMachine = _virtualMachine,
  _thread = _thread,
  _threadReferenceType = _threadReferenceType,
  _location = _location
) with WatchpointEventInfo {
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
  override def toJavaInfo: WatchpointEventInfo = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newWatchpointEventInfo(
      scalaVirtualMachine = scalaVirtualMachine,
      watchpointEvent = watchpointEvent,
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
  override def toJdiInstance: WatchpointEvent = watchpointEvent

  /**
   * Returns the field that is about to be accessed/modified.
   *
   * @return The information profile about the field
   */
  override def field: FieldVariableInfo = infoProducer.newFieldInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    container = _container,
    field = _field,
    offsetIndex = -1
  )(
    virtualMachine = _virtualMachine
  )

  /**
   * Returns the object whose field is about to be accessed/modified.
   *
   * @return Some information profile about the object if the field is from
   *         an instance of an object, otherwise None if the field is
   *         static
   */
  override def `object`: Option[ObjectInfo] = {
    Option(watchpointEvent.`object`()).map(o =>
      infoProducer.newObjectInfo(
        scalaVirtualMachine = scalaVirtualMachine,
        objectReference = o
      )(
        virtualMachine = _virtualMachine,
        referenceType = o.referenceType()
      )
    )
  }

  /**
   * Returns the value of the field that is about to be accessed/modified.
   *
   * @return The information profile about the value
   */
  override def currentValue: ValueInfo = infoProducer.newValueInfo(
    scalaVirtualMachine = scalaVirtualMachine,
    value = watchpointEvent.valueCurrent()
  )
}
