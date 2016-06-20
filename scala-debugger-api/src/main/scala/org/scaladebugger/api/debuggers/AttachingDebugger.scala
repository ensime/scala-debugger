package org.scaladebugger.api.debuggers
import acyclic.file

import com.sun.jdi._
import com.sun.jdi.connect.AttachingConnector
import org.scaladebugger.api.profiles.{ProfileManager, StandardProfileManager}
import org.scaladebugger.api.utils.{Logging, LoopingTaskRunner}
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, StandardScalaVirtualMachine}

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
    new StandardProfileManager,
    new LoopingTaskRunner(),
    port = port,
    hostname = hostname,
    timeout = timeout
  )
}

/**
 * Represents a debugger that attaches to a remote JVM via a socket.
 *
 * @param virtualMachineManager The manager to use for virtual machine
 *                              connectors
 * @param profileManager The manager of profiles to use with the
 *                       ScalaVirtualMachine created from the attached VM
 * @param loopingTaskRunner The task runner to use with the ScalaVirtualMachine
 *                          created from the attached VM
 * @param port The port to use to attach to the JVM
 * @param hostname Optional hostname of the JVM to attach to
 * @param timeout Optional timeout in milliseconds when attaching
 */
class AttachingDebugger private[api] (
  private val virtualMachineManager: VirtualMachineManager,
  private val profileManager: ProfileManager,
  private val loopingTaskRunner: LoopingTaskRunner,
  private val port: Int,
  private val hostname: String = "",
  private val timeout: Long = 0
) extends Debugger with Logging {
  private val ConnectorClassString = "com.sun.jdi.SocketAttach"
  @volatile private var scalaVirtualMachine: Option[ScalaVirtualMachine] = None

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  override def isRunning: Boolean = scalaVirtualMachine.nonEmpty

  /**
   * Retrieves the process of the attached JVM.
   *
   * @return The Java process representing the attached JVM
   */
  def process: Option[Process] =
    scalaVirtualMachine.map(_.underlyingVirtualMachine).map(_.process())

  /**
   * Starts the debugger, resulting in attaching a new process to connect to.
   *
   * @param defaultProfile The default profile to use with the new VMs
   * @param startProcessingEvents If true, events are immediately processed by
   *                              the VM as soon as it is connected
   * @param newVirtualMachineFunc The function to be invoked once the process
   *                              has been attached
   * @tparam T The return type of the callback function
   */
  override def start[T](
    defaultProfile: String,
    startProcessingEvents: Boolean,
    newVirtualMachineFunc: ScalaVirtualMachine => T
  ): Unit = {
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
    val virtualMachine = connector.attach(arguments)

    logger.debug("Starting looping task runner")
    loopingTaskRunner.start()

    scalaVirtualMachine = Some(newScalaVirtualMachine(
      virtualMachine,
      profileManager,
      loopingTaskRunner
    ))


    getPendingScalaVirtualMachines.foreach(
      scalaVirtualMachine.get.processPendingRequests
    )
    scalaVirtualMachine.get.initialize(
      defaultProfile = defaultProfile,
      startProcessingEvents = startProcessingEvents
    )
    newVirtualMachineFunc(scalaVirtualMachine.get)
  }

  /**
   * Stops the process attached by the debugger.
   */
  override def stop(): Unit = {
    assert(isRunning, "Debugger has not been started!")

    // Stop the looping task runner processing events
    loopingTaskRunner.stop()

    // Free up the connection to the JVM
    scalaVirtualMachine.map(_.underlyingVirtualMachine).foreach(_.dispose())

    // Wipe our reference to the old virtual machine
    scalaVirtualMachine = None
  }

  /**
   * Creates a new ScalaVirtualMachine instance.
   *
   * @param virtualMachine The underlying virtual machine
   * @param profileManager The profile manager associated with the
   *                       virtual machine
   * @param loopingTaskRunner The looping task runner used to process events
   *                          for the virtual machine
   *
   * @return The new ScalaVirtualMachine instance
   */
  protected def newScalaVirtualMachine(
    virtualMachine: VirtualMachine,
    profileManager: ProfileManager,
    loopingTaskRunner: LoopingTaskRunner
  ): StandardScalaVirtualMachine = new StandardScalaVirtualMachine(
    virtualMachine,
    profileManager,
    loopingTaskRunner
  )

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

  /**
   * Retrieves the connected virtual machines for the debugger.
   *
   * @return The collection of connected virtual machines
   */
  override def connectedScalaVirtualMachines: Seq[ScalaVirtualMachine] =
    scalaVirtualMachine.toSeq
}
