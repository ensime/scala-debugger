package test

import java.io.IOException

import org.scaladebugger.api.utils.{JDITools, Logging}
import org.scaladebugger.tool.Repl
import org.scaladebugger.tool.backend.StateManager
import org.scaladebugger.tool.frontend.VirtualTerminal
import org.scalatest.Matchers

import scala.util.Try

/**
 * Provides fixture methods to provide CLI tools connecting to remote JVMs.
 */
trait ToolFixtures extends TestUtilities with Logging { this: Matchers =>
  /**
   * Creates a new JVM process with the specified class and arguments.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param suspend If true, suspends the process until a debugger connects
   * @param testCode The test code to evaluate when the process has been started
   *                 (provided the open debug port of the process)
   */
  def withProcessPort(
    className: String,
    arguments: Seq[String] = Nil,
    suspend: Boolean = true
  )(testCode: (Int) => Any): Unit = {
    var process: Option[Process] = None
    try {
      val port = JDITools.findOpenPort()
      if (port.isEmpty)
        throw new IOException("No debug port available for JVM process!")

      process = port.map(p => JDITools.spawn(
        className,
        p,
        args = arguments,
        suspend = suspend,
        options = Seq("-Xms32M", "-Xmx64M")
      ))

      // NOTE: We need a delay to prevent the scenario where the process is
      //       not fully ready to accept a condition
      // TODO: Update our delay to either
      //           a) check when the process is ready by polling the port
      //           b) scale with the machine in some manner
      Thread.sleep(2000)

      // If the process has already died, we know there is an issue
      // exitValue() throws IllegalThreadStateException if process not dead
      if (Try(process.get.exitValue()).isSuccess)
        throw new IOException(s"DEAD: $className with ${arguments.mkString(",")}")

      port.foreach(testCode)
    } finally {
      process.foreach(_.destroy())
    }
  }

  /**
   * Creates a new JVM process with the specified class and arguments. The
   * process cannot start suspended.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param testCode The test code to evaluate when the process has been started
   *                 (provided the pid of the process)
   */
  def withProcessPid(
    className: String,
    arguments: Seq[String] = Nil
  )(testCode: (Int) => Any): Unit = {
    var process: Option[Process] = None
    try {
      val (pid, proc) = JDITools.spawnAndGetPid(
        className = className,
        port = 0, // Assign ephemeral port
        args = arguments,
        options = Seq("-Xms32M", "-Xmx64M")
      )

      process = Some(proc)

      // If unable to retrieve the process PID, exit now
      if (pid <= 0) throw new IOException("Unable to retrieve process PID!")

      // NOTE: We need a delay to prevent the scenario where the process is
      //       not fully ready to accept a condition
      // TODO: Update our delay to either
      //           a) check when the process is ready by polling the port
      //           b) scale with the machine in some manner
      Thread.sleep(2000)

      // If the process has already died, we know there is an issue
      // exitValue() throws IllegalThreadStateException if process not dead
      if (Try(process.get.exitValue()).isSuccess)
        throw new IOException(s"DEAD: $className with ${arguments.mkString(",")}")

      testCode(pid)
    } finally {
      process.foreach(_.destroy())
    }
  }

  /**
   * Creates a new JVM process with the specified class and arguments. Then,
   * launches a virtual terminal to simulate the REPL.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param waitTime Maximum time (in milliseconds) to wait for new input into
   *                 the tool's terminal before terminating
   * @param autoStart If true, automatically starts the REPL process, otherwise
   *                  the start function must be executed
   * @param testCode The test code to evaluate when the process has been
   *                 started (provided a virtual terminal connected to a
   *                 running tool, the underlying state manager for the REPL,
   *                 and a start function that should be invoked
   *                 if autoStart is false)
   */
  def withToolRunning(
    className: String,
    arguments: Seq[String] = Nil,
    waitTime: Long = VirtualTerminal.DefaultWaitTime,
    autoStart: Boolean = true
  )(testCode: (VirtualTerminal, StateManager, () => Unit) => Any): Unit = {
    withToolRunningUsingTerminal(
      className = className,
      arguments = arguments,
      virtualTerminal = new VirtualTerminal(waitTime = waitTime),
      autoStart = autoStart
    )(testCode)
  }

  /**
   * Creates a new JVM process with the specified class and arguments. Then,
   * launches a virtual terminal to simulate the REPL.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param virtualTerminal The virtual terminal to associate with the REPL
   *                        created by this fixture
   * @param autoStart If true, automatically starts the REPL process, otherwise
   *                  the start function must be executed
   * @param testCode The test code to evaluate when the process has been
   *                 started (provided a virtual terminal connected to a
   *                 running tool, the underlying state manager for the REPL,
   *                 and a start function that should be invoked
   *                 if autoStart is false)
   */
  def withToolRunningUsingTerminal(
    className: String,
    virtualTerminal: VirtualTerminal,
    arguments: Seq[String] = Nil,
    autoStart: Boolean = true
  )(testCode: (VirtualTerminal, StateManager, () => Unit) => Any): Unit = {
    withProcessPort(className, arguments) { (port) =>
      var repl: Option[Repl] = None
      try {
        repl = Some(Repl.newInstance(mainTerminal = virtualTerminal))

        // Queue up attach action
        virtualTerminal.newInputLine(s"attach $port")

        // Create start function to begin running the repl
        val startFunc = () => repl.foreach(_.start())

        // Start processing input if auto-starting
        if (autoStart) startFunc()

        // Execute test code
        repl.map(_.stateManager)
          .foreach(testCode(virtualTerminal, _, startFunc))
      } finally {
        repl.foreach(_.stop())
      }
    }
  }
}
