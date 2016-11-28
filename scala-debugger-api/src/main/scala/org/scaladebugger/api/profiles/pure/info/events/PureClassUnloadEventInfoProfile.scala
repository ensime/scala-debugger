package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event.ClassUnloadEvent
import com.sun.jdi.{ReferenceType, ThreadReference, VirtualMachine}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.events.ClassUnloadEventInfoProfile
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, ReferenceTypeInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a class unload event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param classUnloadEvent The class unload event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 */
class PureClassUnloadEventInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val classUnloadEvent: ClassUnloadEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
) extends PureEventInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  event = classUnloadEvent,
  jdiArguments = jdiArguments
) with ClassUnloadEventInfoProfile {
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
  override def toJavaInfo: ClassUnloadEventInfoProfile = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newClassUnloadEventInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      classUnloadEvent = classUnloadEvent,
      jdiArguments = jdiArguments
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: ClassUnloadEvent = classUnloadEvent

  /**
   * Returns the name of the class being unloaded.
   *
   * @return The fully-qualified class name
   */
  override def className: String = classUnloadEvent.className()

  /**
   * Returns the signature of the class being unloaded.
   *
   * @return The JNI-style signature of the class
   */
  override def classSignature: String = classUnloadEvent.classSignature()
}
