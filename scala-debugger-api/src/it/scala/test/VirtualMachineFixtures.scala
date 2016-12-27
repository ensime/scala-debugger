package test

import org.scaladebugger.api.debuggers.LaunchingDebugger
import org.scaladebugger.api.utils.{JDITools, Logging}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ControlledParallelSuite

/**
 * Provides fixture methods to provide virtual machines running specified
 * files.
 */
trait VirtualMachineFixtures extends ApiTestUtilities with Logging {
  this: ControlledParallelSuite =>

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
  ): Unit = semaSync("withLazyVirtualMachine") {
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
        // NOTE: Hack to get processes to exit
        // TODO: Doesn't seem to affect overall test run via it:test
        Thread.sleep(1000)
        launchingDebugger.stop()
      }
    })
  }
}
