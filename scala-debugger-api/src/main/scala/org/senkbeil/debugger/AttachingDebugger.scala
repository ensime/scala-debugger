package org.senkbeil.debugger

import org.senkbeil.utils.LogLike
import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector

import scala.collection.JavaConverters._

object AttachingDebugger {
  /**
   * Creates a new instance of the attaching debugger.
   *
   * @param port The port to use to attach to the JVM
   * @param hostname Optional hostname of the JVM to attach to
   * @param timeout Optional timeout in milliseconds when attaching
   * @param virtualMachineManager The manager to use for virtual machine
   *                              connectors
   */
  def apply(
    port: Int,
    hostname: String = "",
    timeout: Long = 0
  )(implicit virtualMachineManager: VirtualMachineManager =
    Bootstrap.virtualMachineManager()
  ) = new AttachingDebugger(
    virtualMachineManager,
    port = port,
    hostname = hostname,
    timeout = timeout
  )
}

/**
 * Represents a debugger that starts a new process on the same machine.
 *
 * @param virtualMachineManager The manager to use for virtual machine
 *                              connectors
 * @param port The port to use to attach to the JVM
 * @param hostname Optional hostname of the JVM to attach to
 * @param timeout Optional timeout in milliseconds when attaching
 */
class AttachingDebugger private[debugger] (
  private val virtualMachineManager: VirtualMachineManager,
  private val port: Int,
  private val hostname: String = "",
  private val timeout: Long = 0
) extends Debugger with LogLike {
  private val ConnectorClassString = "com.sun.jdi.SocketAttach"
  @volatile private var virtualMachine: Option[VirtualMachine] = None

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean = virtualMachine.nonEmpty

  /**
   * Retrieves the process of the attached JVM.
   *
   * @return The Java process representing the attached JVM
   */
  def process: Option[Process] = virtualMachine.map(_.process())

  /**
   * Starts the debugger, resulting in attaching a new process to connect to.
   *
   * @param newVirtualMachineFunc The function to be invoked once the process
   *                              has been attached
   * @tparam T The return type of the callback function
   */
  def start[T](newVirtualMachineFunc: VirtualMachine => T): Unit = {
    assert(!isRunning, "Debugger already started!")
    assertJdiLoaded()

    // Retrieve the attaching connector, or throw an exception if failed
    val connector = findAttachingConnector.getOrElse(
      throw new AssertionError("Unable to retrieve connector!"))

    val arguments = connector.defaultArguments()

    val _hostname =
      if (hostname != null && hostname.nonEmpty) hostname
      else arguments.get("hostname").value()
    val _port = port.toString
    val _timeout =
      if (timeout > 0) timeout.toString
      else arguments.get("timeout").value()

    arguments.get("hostname").setValue(_hostname)
    arguments.get("port").setValue(_port)
    arguments.get("timeout").setValue(_timeout)

    logger.info("Attaching hostname: " + _hostname)
    logger.info("Attaching port: " + _port)
    logger.info("Attaching timeout: " + _timeout)
    virtualMachine = Some(connector.attach(arguments))
    newVirtualMachineFunc(virtualMachine.get)
  }

  /**
   * Stops the process attached by the debugger.
   */
  def stop(): Unit = {
    assert(isRunning, "Debugger has not been started!")

    // Free up the connection to the JVM
    virtualMachine.get.dispose()

    // Wipe our reference to the old virtual machine
    virtualMachine = None
  }

  /**
   * Retrieves the connector to be used to attach a new process and connect
   * to it.
   *
   * @return Some connector if available, otherwise None
   */
  private def findAttachingConnector: Option[AttachingConnector] = {
    virtualMachineManager.attachingConnectors().asScala
      .find(_.name() == ConnectorClassString)
  }
}
