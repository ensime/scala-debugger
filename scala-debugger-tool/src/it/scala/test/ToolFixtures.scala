package test

import java.io.IOException

import org.scaladebugger.api.utils.{JDITools, Logging}
import org.scaladebugger.tool.Repl

/**
 * Provides fixture methods to provide CLI tools connecting to remote JVMs.
 */
trait ToolFixtures extends TestUtilities with Logging {
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
        suspend = suspend
      ))

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
        args = arguments
      )

      process = Some(proc)

      // If unable to retrieve the process PID, exit now
      if (pid <= 0) throw new IOException("Unable to retrieve process PID!")

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
   * @param testCode The test code to evaluate when the process has been started
   */
  def withToolRunning(
    className: String,
    arguments: Seq[String] = Nil
  )(testCode: (VirtualTerminal) => Any): Unit = {
    withProcessPort(className, arguments) { (port) =>
      var repl: Option[Repl] = None
      try {
        val terminal = new VirtualTerminal()

        repl = Some(Repl.newInstance(mainTerminal = terminal))

        // Queue up attach action
        terminal.newInputLine(s"attach $port")

        // Start processing input
        repl.foreach(_.start())

        // Execute test code
        testCode(terminal)
      } finally {
        repl.foreach(_.stop())
      }
    }
  }
}
