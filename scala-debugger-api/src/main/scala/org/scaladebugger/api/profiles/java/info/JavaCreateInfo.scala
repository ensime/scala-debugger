package org.scaladebugger.api.profiles.java.info


import com.sun.jdi.{ReferenceType, Value}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.utils.JDIHelperMethods
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a java profile for creating data that adds no extra logic
 * on top of the standard JDI.
 */
trait JavaCreateInfo extends CreateInfo with JDIHelperMethods {
  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducer

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  override def createRemotely(value: AnyVal): ValueInfo = {
    import org.scaladebugger.api.lowlevel.wrappers.Implicits._
    createNewValueProfile(_virtualMachine.mirrorOf(value))
  }

  /**
   * Creates the provided value on the remote JVM.
   *
   * @param value The value to create (mirror) on the remote JVM
   * @return The information about the remote value
   */
  override def createRemotely(value: String): ValueInfo = {
    createNewValueProfile(_virtualMachine.mirrorOf(value))
  }

  protected def createNewValueProfile(value: Value): ValueInfo =
    infoProducer.newValueInfo(scalaVirtualMachine, value)
}
