package org.scaladebugger.tool.backend.functions
import acyclic.file
import org.scaladebugger.api.debuggers.{AttachingDebugger, LaunchingDebugger, ListeningDebugger, ProcessDebugger}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.tool.backend.StateManager

/**
 * Represents a collection of functions for starting debuggers.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class DebuggerFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for attaching to a running JVM. */
  def attach(m: Map[String, Any]) = {
    val port = m.getOrElse(
      "port",
      throw new RuntimeException("Missing port argument!")
    ).toString.toDouble.toInt
    val hostname = m.getOrElse("hostname", "localhost").toString
    val timeout = m.getOrElse("timeout", 0).toString.toDouble.toInt

    val d = AttachingDebugger(port, hostname, timeout)
      .withPending(stateManager.state.dummyScalaVirtualMachine)
    stateManager.updateActiveDebugger(d)

    d.start(s => {
      writeLine("Attached with id " + s.uniqueId)
      stateManager.addScalaVirtualMachine(s)
    })
  }

  /** Entrypoint for attaching to a running JVM using its pid. */
  def attachp(m: Map[String, Any]) = {
    val pid = m.getOrElse(
      "pid",
      throw new RuntimeException("Missing pid argument!")
    ).toString.toDouble.toInt
    val timeout = m.getOrElse("timeout", 0).toString.toDouble.toInt

    val d = ProcessDebugger(pid, timeout)
      .withPending(stateManager.state.dummyScalaVirtualMachine)
    stateManager.updateActiveDebugger(d)

    d.start(s => {
      writeLine("Attached with id " + s.uniqueId)
      stateManager.addScalaVirtualMachine(s)
    })
  }

  /** Entrypoint for launching and attaching to a JVM. */
  def launch(m: Map[String, Any]) = {
    val className = m.getOrElse(
      "class",
      throw new RuntimeException("Missing class argument!")
    ).toString
    val suspend = m.getOrElse("suspend", true).toString.toBoolean

    val d = LaunchingDebugger(
      className,
      jvmOptions = Seq("-classpath", JDITools.jvmClassPath),
      suspend = suspend
    ).withPending(stateManager.state.dummyScalaVirtualMachine)
    stateManager.updateActiveDebugger(d)

    d.start(s => {
      writeLine("Attached with id " + s.uniqueId)
      stateManager.addScalaVirtualMachine(s)
    })
  }

  /** Entrypoint for listening and receiving connections from JVMs. */
  def listen(m: Map[String, Any]) = {
    val port = m.getOrElse(
      "port",
      throw new RuntimeException("Missing port argument!")
    ).toString.toDouble.toInt
    val hostname = m.getOrElse("hostname", "localhost").toString

    val d = ListeningDebugger(port, hostname)
      .withPending(stateManager.state.dummyScalaVirtualMachine)
    stateManager.updateActiveDebugger(d)

    d.start(s => {
      writeLine("Received connection from JVM with id " + s.uniqueId)
      stateManager.addScalaVirtualMachine(s)
    })
  }

  /** Entrypoint for setting or displaying the current profile name. */
  def profile(m: Map[String, Any]) = m.get("name") match {
    case Some(name) => stateManager.updateActiveProfile(name.toString)
    case None       => writeLine(stateManager.state.activeProfileName)
  }

  /** Entrypoint for displaying profile name choices. */
  def profiles(m: Map[String, Any]) = {
    // TODO: Pull names from profile manager?
    val names = Seq(PureDebugProfile.Name, Scala210DebugProfile.Name)
    writeLine(names.map("-> " + _).mkString("\n"))
  }

  /** Entrypoint for stopping the current debugger. */
  def stop(m: Map[String, Any]) = {
    stateManager.clear()
  }
}
