package test

import org.senkbeil.debugger.LaunchingDebugger
import org.senkbeil.debugger.events.LoopingTaskRunner
import org.senkbeil.debugger.events.EventType._
import org.senkbeil.debugger.virtualmachines.ScalaVirtualMachine
import org.senkbeil.utils.LogLike
import com.sun.jdi.VirtualMachine

import scala.concurrent.Future
import scala.util.Try

/**
 * Provides fixture methods to provide virtual machines running specified
 * files.
 */
trait VirtualMachineFixtures extends TestUtilities with LogLike {
  def withVirtualMachine(
    className: String,
    arguments: Seq[String] = Nil,
    suspend: Boolean = true,
    timeout: Long = 1000
  )(
    testCode: (VirtualMachine, ScalaVirtualMachine) => Any
  ) = {
    val launchingDebugger = LaunchingDebugger(
      className = className,
      commandLineArguments = arguments,
      jvmOptions = Seq("-classpath", jvmClasspath),
      suspend = true // This should always be true for our tests, using resume
                     // above to indicate whether should resume after start
    )
    val loopingTaskRunner = new LoopingTaskRunner()

    launchingDebugger.start { (virtualMachine) =>
      // Redirect output of JVM to our logs
      val process = virtualMachine.process()

      import scala.concurrent.ExecutionContext.Implicits.global
      Future {
        val path = java.nio.file.Files.createTempFile("jvmfixture", ".out.log")
        logger.debug(s"Creating JVM Output File: ${path.toString}")

        Try(java.nio.file.Files.copy(process.getInputStream, path,
          java.nio.file.StandardCopyOption.REPLACE_EXISTING))

        // NOTE: Comment me out to keep around the log file
        logger.debug(s"Deleting JVM Output File: ${path.toString}")
        java.nio.file.Files.delete(path)
      }
      Future {
        val path = java.nio.file.Files.createTempFile("jvmfixture", ".err.log")
        logger.debug(s"Creating JVM Error File: ${path.toString}")

        Try(java.nio.file.Files.copy(process.getErrorStream, path,
          java.nio.file.StandardCopyOption.REPLACE_EXISTING))

        // NOTE: Comment me out to keep around the log file
        logger.debug(s"Deleting JVM Error File: ${path.toString}")
        java.nio.file.Files.delete(path)
      }

      try {
        val scalaVirtualMachine = new ScalaVirtualMachine(
          virtualMachine,
          loopingTaskRunner
        )

        val eventManager = scalaVirtualMachine.eventManager

        // Wait for connection event to run the test code (ensures everything
        // is ready)
        var virtualMachineReady = false
        eventManager.addEventHandler(VMStartEventType, _ => {
          logger.debug("Received start event for test, marking ready!")
          virtualMachineReady = true

          // Only resume the virtual machine IF we did not request it suspended
          logger.debug(s"Resuming vm: ${!suspend}")
          !suspend
        })

        loopingTaskRunner.start()
        while (!virtualMachineReady) { Thread.sleep(1) }

        testCode(virtualMachine, scalaVirtualMachine)
      } finally {
        loopingTaskRunner.stop()
        launchingDebugger.stop()
      }
    }
  }
}
