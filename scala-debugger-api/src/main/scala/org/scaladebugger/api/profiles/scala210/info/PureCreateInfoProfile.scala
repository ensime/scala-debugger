package org.scaladebugger.api.profiles.scala210.info

//import acyclic.file

import com.sun.jdi.Value
import org.scaladebugger.api.lowlevel.utils.JDIHelperMethods
import org.scaladebugger.api.profiles.traits.info.{CreateInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a pure profile for creating data that adds no extra logic
 * on top of the standard JDI.
 */
trait PureCreateInfoProfile extends CreateInfoProfile with JDIHelperMethods {
  protected val scalaVirtualMachine: ScalaVirtualMachine

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  override def createRemotely(value: AnyVal): ValueInfoProfile = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    createNewValueProfile(_virtualMachine.mirrorOf(value))
  }

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  override def createRemotely(value: String): ValueInfoProfile = {
    createNewValueProfile(_virtualMachine.mirrorOf(value))
  }

  protected def createNewValueProfile(value: Value): ValueInfoProfile =
    new PureValueInfoProfile(scalaVirtualMachine, value)
}
