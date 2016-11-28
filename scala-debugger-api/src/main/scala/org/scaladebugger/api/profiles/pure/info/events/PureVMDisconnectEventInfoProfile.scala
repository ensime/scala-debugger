package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event.VMDisconnectEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.VMDisconnectEventInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a vm disconnect event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param vmDisconnectEvent The vm disconnect event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 */
class PureVMDisconnectEventInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val vmDisconnectEvent: VMDisconnectEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
) extends PureEventInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  event = vmDisconnectEvent,
  jdiArguments = jdiArguments
) with VMDisconnectEventInfoProfile {
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
  override def toJavaInfo: VMDisconnectEventInfoProfile = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newVMDisconnectEventInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      vmDisconnectEvent = vmDisconnectEvent,
      jdiArguments = jdiArguments
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: VMDisconnectEvent = vmDisconnectEvent
}
