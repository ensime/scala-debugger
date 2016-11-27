package org.scaladebugger.api.profiles.pure.info.events

import com.sun.jdi.event.VMDeathEvent
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile
import org.scaladebugger.api.profiles.traits.info.events.VMDeathEventInfoProfile
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure implementation of a vm death event info
 * profile that adds no custom logic on top of the standard JDI.
 *
 * @param scalaVirtualMachine The high-level virtual machine containing the
 *                            array
 * @param infoProducer The producer of info-based profile instances
 * @param vmDeathEvent The vm death event to wrap in the profile
 * @param jdiArguments The request and event arguments tied to the provided
 *                     event
 */
class PureVMDeathEventInfoProfile(
  override val scalaVirtualMachine: ScalaVirtualMachine,
  override protected val infoProducer: InfoProducerProfile,
  private val vmDeathEvent: VMDeathEvent,
  private val jdiArguments: Seq[JDIArgument] = Nil
) extends PureEventInfoProfile(
  scalaVirtualMachine = scalaVirtualMachine,
  infoProducer = infoProducer,
  event = vmDeathEvent,
  jdiArguments = jdiArguments
) with VMDeathEventInfoProfile {
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
  override def toJavaInfo: VMDeathEventInfoProfile = {
    val jep = infoProducer.eventProducer.toJavaInfo
    jep.newVMDeathEventInfoProfile(
      scalaVirtualMachine = scalaVirtualMachine,
      vmDeathEvent = vmDeathEvent
    )
  }

  /**
   * Returns the JDI representation this profile instance wraps.
   *
   * @return The JDI instance
   */
  override def toJdiInstance: VMDeathEvent = vmDeathEvent
}
