package test

import java.io.IOException

import org.scaladebugger.api.utils.{JDITools, Logging}
import org.scaladebugger.tool.Repl
import org.scaladebugger.tool.frontend.Terminal

/**
 * Provides fixture methods to provide CLI tools connecting to
 */
trait ToolFixtures extends TestUtilities with Logging {
  /**
   * Creates a new JVM process with the specified class and arguments.
   *
   * @param className The name of the main class to use as the JVM entrypoint
   * @param arguments The arguments to provide to the main class
   * @param suspend If true, suspends the process until a debugger connects
   * @param testCode The test code to evaluate when the process has been started
   */
  def withProcess(
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
   *
   * @param className
   * @param arguments
   * @param testCode
   */
  def withToolRunning(
    className: String,
    arguments: Seq[String] = Nil
  )(testCode: (Terminal) => Any): Unit = {
    withProcess(className, arguments) { (port) =>
      var repl: Option[Repl] = None
      try {
        val terminal = new VirtualTerminal()

        repl = Some(Repl
          .newInstance(forceUseFallback = false)
          .withTerminal(terminal))

        repl.map(_.activeTerminal).foreach(testCode)
      } finally {
        repl.foreach(_.stop())
      }
    }
  }
}
