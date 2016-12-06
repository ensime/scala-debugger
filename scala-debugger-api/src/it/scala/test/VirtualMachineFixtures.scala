package test

import java.io.{BufferedReader, InputStreamReader}
import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.VMStartEvent
import org.scaladebugger.api.debuggers.LaunchingDebugger
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.lowlevel.events.EventType
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.utils.{JDITools, Logging, LoopingTaskRunner}
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, StandardScalaVirtualMachine}
import EventType._
import com.sun.jdi.VirtualMachine
import org.scalatest.concurrent.Eventually

import scala.concurrent.Future
import scala.io.Source
import scala.util.Try

/**
 * Provides fixture methods to provide virtual machines running specified
 * files.
 */
trait VirtualMachineFixtures extends TestUtilities with Logging {
  /**
   * Creates a new virtual machine with the specified class and arguments.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param pendingScalaVirtualMachines The collection of virtual machines
   *                                    containing pending requests to apply
   *                                    to the generated virtual machine
   * @param testCode The test code to evaluate when the JVM connects, given the
   *                 ScalaVirtualMachine as an argument
   */
  def withVirtualMachine(
    className: String,
    arguments: Seq[String] = Nil,
    pendingScalaVirtualMachines: Seq[ScalaVirtualMachine] = Nil
  )(
    testCode: (ScalaVirtualMachine) => Any
  ): Unit = {
    val pendings = pendingScalaVirtualMachines
    withLazyVirtualMachine(className, arguments, pendings) { (s, start) =>
      start()
      testCode(s)
    }
  }

  /**
   * Creates a new virtual machine with the specified class and arguments.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param pendingScalaVirtualMachines The collection of virtual machines
   *                                    containing pending requests to apply
   *                                    to the generated virtual machine
   * @param testCode The test code to evaluate when the JVM connects, given
   *                 the ScalaVirtualMachine and a function to execute when
   *                 ready to start the virtual machine
   */
  def withLazyVirtualMachine(
    className: String,
    arguments: Seq[String] = Nil,
    pendingScalaVirtualMachines: Seq[ScalaVirtualMachine] = Nil
  )(
    testCode: (ScalaVirtualMachine, () => Unit) => Any
  ): Unit = {
    val launchingDebugger = LaunchingDebugger(
      className             = className,
      commandLineArguments  = arguments,
      jvmOptions            = Seq("-classpath", JDITools.jvmClassPath),
      suspend               = true // This should always be true for our tests
    )

    // Apply any pending requests
    pendingScalaVirtualMachines.foreach(
      launchingDebugger.addPendingScalaVirtualMachine
    )

    launchingDebugger.start(startProcessingEvents = false, { s =>
      try {
        val startFunc = () => s.startProcessingEvents()

        testCode(s, startFunc)
      } finally {
        launchingDebugger.stop()
      }
    })
  }
}
