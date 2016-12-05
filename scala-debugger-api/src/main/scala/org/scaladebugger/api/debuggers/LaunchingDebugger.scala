package org.scaladebugger.api.debuggers
import com.sun.jdi._
import com.sun.jdi.connect.LaunchingConnector
import org.scaladebugger.api.profiles.{ProfileManager, StandardProfileManager}
import org.scaladebugger.api.utils.{Logging, LoopingTaskRunner}
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, ScalaVirtualMachineManager, StandardScalaVirtualMachine}

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
    new StandardProfileManager,
    new LoopingTaskRunner(),
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
 * @param profileManager The manager of profiles to use with the
 *                       ScalaVirtualMachine created from the launched VM
 * @param loopingTaskRunner The task runner to use with the ScalaVirtualMachine
 *                          created from the launched VM
 * @param className The name of the class to use as the entrypoint for the new
 *                  process
 * @param commandLineArguments The command line arguments to provide to the new
 *                             process
 * @param jvmOptions The options to provide to the new process' JVM
 * @param suspend If true, suspends the JVM until it connects to the debugger
 */
class LaunchingDebugger private[api] (
  private val virtualMachineManager: VirtualMachineManager,
  private val profileManager: ProfileManager,
  private val loopingTaskRunner: LoopingTaskRunner,
  private val className: String,
  private val commandLineArguments: Seq[String] = Nil,
  private val jvmOptions: Seq[String] = Nil,
  private val suspend: Boolean = true
) extends Debugger with Logging {
  private val ConnectorClassString = "com.sun.jdi.CommandLineLaunch"

  /** Represents the active Scala virtual machine. */
  @volatile private var scalaVirtualMachine: Option[ScalaVirtualMachine] = None

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  override def isRunning: Boolean = scalaVirtualMachine.nonEmpty

  /**
   * Retrieves the process of the launched JVM.
   *
   * @return The Java process representing the launched JVM
   */
  def process: Option[Process] =
    scalaVirtualMachine.map(_.underlyingVirtualMachine).map(_.process())

  /**
   * Starts the debugger, resulting in launching a new process to connect to.
   *
   * @param defaultProfile The default profile to use with the new VMs
   * @param startProcessingEvents If true, events are immediately processed by
   *                              the VM as soon as it is connected
   * @param newVirtualMachineFunc The function to be invoked once the process
   *                              has been launched
   * @tparam T The return type of the callback function
   */
  override def start[T](
    defaultProfile: String,
    startProcessingEvents: Boolean,
    newVirtualMachineFunc: ScalaVirtualMachine => T
  ): Unit = synchronized {
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
    val virtualMachine = connector.launch(arguments)

    logger.debug("Starting looping task runner")
    loopingTaskRunner.start()

    // Create and set our new active SVM
    scalaVirtualMachine = Some(addNewScalaVirtualMachine(
      scalaVirtualMachineManager,
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
   * Stops the process launched by the debugger.
   */
  override def stop(): Unit = synchronized {
    assert(isRunning, "Debugger has not been started!")

    // Stop the looping task runner processing events
    loopingTaskRunner.stop()

    // TODO: Investigate why dispose throws a VMDisconnectedException
    // Invalidate the virtual machine mirror
    //virtualMachine.get.dispose()

    // Kill the process associated with the local virtual machine
    logger.info("Shutting down process: " +
      (className +: commandLineArguments).mkString(" "))
    scalaVirtualMachine.map(_.underlyingVirtualMachine)
      .foreach(_.process().destroy())

    // Wipe our reference to the old virtual machine
    scalaVirtualMachine.foreach(scalaVirtualMachineManager.remove)
    scalaVirtualMachine = None
  }

  /**
   * Creates and adds a new ScalaVirtualMachine instance.
   *
   * @param scalaVirtualMachineManager The manager of of the new virtual machine
   * @param virtualMachine The underlying virtual machine
   * @param profileManager The profile manager associated with the
   *                       virtual machine
   * @param loopingTaskRunner The looping task runner used to process events
   *                          for the virtual machine
   * @return The new ScalaVirtualMachine instance
   */
  protected def addNewScalaVirtualMachine(
    scalaVirtualMachineManager: ScalaVirtualMachineManager,
    virtualMachine: VirtualMachine,
    profileManager: ProfileManager,
    loopingTaskRunner: LoopingTaskRunner
  ): ScalaVirtualMachine = {
    scalaVirtualMachineManager.add(new StandardScalaVirtualMachine(
      scalaVirtualMachineManager,
      virtualMachine,
      profileManager,
      loopingTaskRunner
    ))
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
