package org.senkbeil.debugger

import org.senkbeil.utils.LogLike
import com.sun.jdi._
import com.sun.jdi.connect.LaunchingConnector

import scala.collection.JavaConverters._

object LaunchingDebugger {
  /**
   * Creates a new instance of the launching debugger.
   *
   * @param className The name of the class to use as the entrypoint for the
   *                  new process
   * @param commandLineArguments The command line arguments to provide to the
   *                             new process
   * @param jvmOptions The options to provide to the new process' JVM
   * @param suspend If true, suspends the JVM until it connects to the debugger
   * @param virtualMachineManager The manager to use for virtual machine
   *                              connectors
   */
  def apply(
    className: String,
    commandLineArguments: Seq[String] = Nil,
    jvmOptions: Seq[String] = Nil,
    suspend: Boolean = true
  )(implicit virtualMachineManager: VirtualMachineManager =
    Bootstrap.virtualMachineManager()
  ) = new LaunchingDebugger(
    virtualMachineManager,
    className,
    commandLineArguments,
    jvmOptions,
    suspend
  )
}

/**
 * Represents a debugger that starts a new process on the same machine.
 *
 * @param virtualMachineManager The manager to use for virtual machine
 *                              connectors
 * @param className The name of the class to use as the entrypoint for the new
 *                  process
 * @param commandLineArguments The command line arguments to provide to the new
 *                             process
 * @param jvmOptions The options to provide to the new process' JVM
 * @param suspend If true, suspends the JVM until it connects to the debugger
 */
class LaunchingDebugger private[debugger] (
  private val virtualMachineManager: VirtualMachineManager,
  private val className: String,
  private val commandLineArguments: Seq[String] = Nil,
  private val jvmOptions: Seq[String] = Nil,
  private val suspend: Boolean = true
) extends Debugger with LogLike {
  private val ConnectorClassString = "com.sun.jdi.CommandLineLaunch"
  @volatile private var virtualMachine: Option[VirtualMachine] = None

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean = virtualMachine.nonEmpty

  /**
   * Retrieves the process of the launched JVM.
   *
   * @return The Java process representing the launched JVM
   */
  def process: Option[Process] = virtualMachine.map(_.process())

  /**
   * Starts the debugger, resulting in launching a new process to connect to.
   *
   * @param newVirtualMachineFunc The function to be invoked once the process
   *                              has been launched
   * @tparam T The return type of the callback function
   */
  def start[T](newVirtualMachineFunc: VirtualMachine => T): Unit = {
    assert(!isRunning, "Debugger already started!")
    assertJdiLoaded()

    // Retrieve the launching connector, or throw an exception if failed
    val connector = findLaunchingConnector.getOrElse(
      throw new AssertionError("Unable to retrieve connector!"))

    val arguments = connector.defaultArguments()
    val main = (className +: commandLineArguments).mkString(" ")
    val options = (arguments.get("options").value() +: jvmOptions).mkString(" ")

    arguments.get("main").setValue(main)
    arguments.get("options").setValue(options)
    arguments.get("suspend").setValue(suspend.toString)

    logger.info("Launching main: " + main)
    logger.info("Launching options: " + options)
    logger.info("Launching suspend: " + suspend)
    virtualMachine = Some(connector.launch(arguments))
    newVirtualMachineFunc(virtualMachine.get)
  }

  /**
   * Stops the process launched by the debugger.
   */
  def stop(): Unit = {
    assert(isRunning, "Debugger has not been started!")

    // TODO: Investigate why dispose throws a VMDisconnectedException
    // Invalidate the virtual machine mirror
    //virtualMachine.get.dispose()

    // Kill the process associated with the local virtual machine
    logger.info("Shutting down process: " +
      (className +: commandLineArguments).mkString(" "))
    virtualMachine.get.process().destroy()

    // Wipe our reference to the old virtual machine
    virtualMachine = None
  }

  /**
   * Retrieves the connector to be used to launch a new process and connect
   * to it.
   *
   * @return Some connector if available, otherwise None
   */
  private def findLaunchingConnector: Option[LaunchingConnector] = {
    virtualMachineManager.launchingConnectors().asScala
      .find(_.name() == ConnectorClassString)
  }
}
