package org.scaladebugger.api.profiles.scala210

import com.sun.jdi.VirtualMachine
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.java.requests.breakpoints.JavaBreakpointRequest
import org.scaladebugger.api.profiles.java.requests.classes.{JavaClassPrepareRequest, JavaClassUnloadRequest}
import org.scaladebugger.api.profiles.java.requests.events.JavaEventListenerRequest
import org.scaladebugger.api.profiles.java.requests.exceptions.JavaExceptionRequest
import org.scaladebugger.api.profiles.java.info.{JavaCreateInfoProfile, JavaGrabInfoProfile, JavaMiscInfo}
import org.scaladebugger.api.profiles.java.requests.methods.{JavaMethodEntryRequest, JavaMethodExitRequest}
import org.scaladebugger.api.profiles.java.requests.monitors.{JavaMonitorContendedEnterRequest, JavaMonitorContendedEnteredRequest, JavaMonitorWaitRequest, JavaMonitorWaitedRequest}
import org.scaladebugger.api.profiles.java.requests.steps.JavaStepRequest
import org.scaladebugger.api.profiles.java.requests.threads.{JavaThreadDeathRequest, JavaThreadStartRequest}
import org.scaladebugger.api.profiles.java.requests.vm.{JavaVMDeathRequest, JavaVMDisconnectRequest, JavaVMStartRequest}
import org.scaladebugger.api.profiles.java.requests.watchpoints.{JavaAccessWatchpointRequest, JavaModificationWatchpointRequest}
import org.scaladebugger.api.profiles.scala210.info.Scala210InfoProducer
import org.scaladebugger.api.profiles.traits.ManagerContainerDebugProfile
import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Contains information about the java debug profile.
 */
object Scala210DebugProfile {
  val Name: String = "scala-2.10"
}

/**
 * Represents a debug profile that adds Scala 2.10 specific debug logic.
 *
 * @param scalaVirtualMachine The high-level virtual machine using this profile
 * @param managerContainer The container of low-level managers to use as the
 *                         underlying implementation
 * @param _virtualMachine The underlying virtual machine to use for various
 *                        retrieval methods
 * @param infoProducer The producer of information profiles
 */
class Scala210DebugProfile(
  protected val scalaVirtualMachine: ScalaVirtualMachine,
  protected val managerContainer: ManagerContainer
)(
  protected val _virtualMachine: VirtualMachine =
    scalaVirtualMachine.underlyingVirtualMachine,
  protected val infoProducer: InfoProducer =
    new Scala210InfoProducer
) extends ManagerContainerDebugProfile
  with JavaAccessWatchpointRequest
  with JavaBreakpointRequest
  with JavaClassPrepareRequest
  with JavaClassUnloadRequest
  with JavaCreateInfoProfile
  with JavaEventListenerRequest
  with JavaExceptionRequest
  with JavaGrabInfoProfile
  with JavaMethodEntryRequest
  with JavaMethodExitRequest
  with JavaMiscInfo
  with JavaModificationWatchpointRequest
  with JavaMonitorContendedEnteredRequest
  with JavaMonitorContendedEnterRequest
  with JavaMonitorWaitedRequest
  with JavaMonitorWaitRequest
  with JavaStepRequest
  with JavaThreadDeathRequest
  with JavaThreadStartRequest
  with JavaVMStartRequest
  with JavaVMDeathRequest
  with JavaVMDisconnectRequest
