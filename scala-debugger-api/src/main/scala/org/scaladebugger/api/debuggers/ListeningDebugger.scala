package org.scaladebugger.api.debuggers
import acyclic.file
import com.sun.jdi._
import com.sun.jdi.connect.{Connector, ListeningConnector}
import org.scaladebugger.api.profiles.{ProfileManager, StandardProfileManager}
import org.scaladebugger.api.utils.{Logging, LoopingTaskRunner}
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, ScalaVirtualMachineManager, StandardScalaVirtualMachine}

import scala.collection.JavaConverters._
import scala.util.Try

object ListeningDebugger {
  /**
   * Creates a new instance of the listening debugger. Defaults to using a
   * single thread executor with one worker.
   *
   * @param virtualMachineManager The manager to use for virtual machine
   *                              connectors
   * @param port The port to use for remote JVMs to attach to this debugger
   * @param hostname The hostname to use for remote JVMs to attach to this
   *                 debugger
   * @param workers The total number of worker tasks to spawn
   */
  def apply(
    port: Int,
    hostname: String = "localhost",
    workers: Int = 1
  )(
    implicit virtualMachineManager: VirtualMachineManager =
      Bootstrap.virtualMachineManager()
  ): ListeningDebugger = new ListeningDebugger(
    virtualMachineManager,
    () => new StandardProfileManager,
    new LoopingTaskRunner(initialWorkers = workers),
    port,
    hostname,
    workers
  )
}

/**
 * Represents a debugger that listens for connections from remote JVMs.
 *
 * @param virtualMachineManager The manager to use for virtual machine
 *                              connectors
 * @param newProfileManagerFunc The function to be executed per new VM
 *                              connection that generates a profile manager for
 *                              each new ScalaVirtualMachine
 * @param loopingTaskRunner The task runner to use with the ScalaVirtualMachines
 *                          created from the listening VMs
 * @param port The port to use for remote JVMs to attach to this debugger
 * @param hostname The hostname to use for remote JVMs to attach to
 *                 this debugger
 * @param workers The total number of worker tasks to spawn
 */
class ListeningDebugger private[api] (
  private val virtualMachineManager: VirtualMachineManager,
  private val newProfileManagerFunc: () => ProfileManager,
  private val loopingTaskRunner: LoopingTaskRunner,
  private val port: Int,
  private val hostname: String,
  private val workers: Int
) extends Debugger with Logging {
  private val ConnectorClassString = "com.sun.jdi.SocketListen"

  /** Represents the active Scala virtual machines. */
  @volatile private var scalaVirtualMachines: Seq[ScalaVirtualMachine] = Nil

  // Contains all components for the currently-running debugger
  @volatile private var components: Option[(
    LoopingTaskRunner,
    ListeningConnector,
    java.util.Map[String, Connector.Argument]
  )] = None

  /**
   * Represents the JVM options to feed to remote JVMs whom will connect to
   * this debugger.
   */
  val remoteJvmOptions = (
    s"-agentlib:jdwp=transport=dt_socket" ::
    s"server=n" ::
    s"suspend=n" ::
    s"address=$hostname:$port" ::
    Nil).mkString(",")

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  override def isRunning: Boolean = components.nonEmpty

  /**
   * Indicates whether or not the listening debugger supports multiple JVM
   * connections or just a single JVM connecting.
   *
   * @return True if multiple JVMs can connect to this debugger, otherwise false
   */
  def supportsMultipleConnections: Boolean =
    findListeningConnector.exists(_.supportsMultipleConnections())

  /**
   * Starts the debugger, resulting in opening the specified socket to listen
   * for remote JVM connections.
   *
   * @param defaultProfile The default profile to use with the new VMs
   * @param startProcessingEvents If true, events are immediately processed by
   *                              the VM as soon as it is connected
   * @param newVirtualMachineFunc The function to be invoked once per JVM that
   *                              connects to this debugger
   * @tparam T The return type of the callback function
   */
  override def start[T](
    defaultProfile: String,
    startProcessingEvents: Boolean,
    newVirtualMachineFunc: ScalaVirtualMachine => T
  ): Unit = synchronized {
    assert(!isRunning, "Debugger already started!")
    assertJdiLoaded()

    // Retrieve the listening connector, or throw an exception if failed
    val connector = findListeningConnector.getOrElse(
      throw new AssertionError("Unable to retrieve connector!"))

    val arguments = connector.defaultArguments()

    if (hostname.nonEmpty) arguments.get("localAddress").setValue(hostname)
    arguments.get("port").setValue(port.toString)

    logger.info("Multiple Connections Allowed: " +
      connector.supportsMultipleConnections())

    // Open port for listening to JVM connections
    logger.info(s"Listening on $hostname:$port")
    connector.startListening(arguments)

    // Start the task runner to process JVMs
    logger.info("Starting looping task runner")
    loopingTaskRunner.start()

    // Store the connector and arguments (used for shutdown)
    components = Some((loopingTaskRunner, connector, arguments))

    // Start X workers to process connection requests
    logger.info(s"Spawning $workers worker tasks")
    (1 to workers).foreach(_ => loopingTaskRunner.addTask {
      listenTask(
        connector,
        arguments,
        defaultProfile,
        startProcessingEvents,
        newVirtualMachineFunc
      )
    })
  }

  /**
   * Stops listening for incoming connections and shuts down the task runner.
   */
  override def stop(): Unit = synchronized {
    assert(isRunning, "Debugger has not been started!")

    val (loopingTaskRunner, connector, arguments) = components.get

    // Cancel all worker threads via interrupt
    logger.info("Cancelling worker threads")
    loopingTaskRunner.stop()

    // Dispose of any connected virtual machines
    // NOTE: Using toArray to create copy of collection to
    //       avoid ConcurrentModificationException
    connectedVirtualMachines.toArray
      .map(vm => Try(vm.dispose()))
      .filter(_.isFailure)
      .map(_.failed.get)
      .foreach(logger.throwable)

    // Close the listening port
    logger.info(s"Shutting down $hostname:$port")
    connector.stopListening(arguments)

    // Mark that we have completely stopped the debugger
    components = None
    scalaVirtualMachines.foreach(scalaVirtualMachineManager.remove)
    scalaVirtualMachines = Nil
  }

  /**
   * Retrieves the current listing of virtual machines that have connected to
   * this debugger.
   *
   * @return The collection of connected virtual machines
   */
  def connectedVirtualMachines: Seq[VirtualMachine] =
    virtualMachineManager.connectedVirtualMachines().asScala

  /**
   * Checks for an incoming connection, creates a new ScalaVirtualMachine for
   * the connection, and invokes the callback with the new ScalaVirtualMachine
   * instance.
   *
   * @param connector The connector to use when listening
   * @param arguments The arguments for the connector to use when accepting
   *                  new connections
   * @param defaultProfile The default profile to use with the virtual machine
   * @param startProcessingEvents If true, events are immediately processed by
   *                              the VM as soon as it is connected
   * @param newVirtualMachineFunc The callback for the new ScalaVirtualMachine
   * @tparam T The return type of the callback
   */
  protected def listenTask[T](
    connector: ListeningConnector,
    arguments: java.util.Map[String, Connector.Argument],
    defaultProfile: String,
    startProcessingEvents: Boolean,
    newVirtualMachineFunc: ScalaVirtualMachine => T
  ): Unit = {
    val newVirtualMachine = Try(connector.accept(arguments))

    // Invoke our callback upon receiving a new virtual machine
    val scalaVirtualMachine = newVirtualMachine.map(addNewScalaVirtualMachine(
      scalaVirtualMachineManager,
      _: VirtualMachine,
      newProfileManagerFunc(),
      loopingTaskRunner
    ))
    scalaVirtualMachine.foreach(s => {
      scalaVirtualMachines :+= s
      getPendingScalaVirtualMachines.foreach(s.processPendingRequests)
      s.initialize(
        defaultProfile = defaultProfile,
        startProcessingEvents = startProcessingEvents
      )
    })
    scalaVirtualMachine.foreach(newVirtualMachineFunc)

    // Release CPU
    Thread.sleep(1)
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
   * Retrieves the connector to be used to listen for incoming JVM connections.
   *
   * @return Some connector if available, otherwise None
   */
  private def findListeningConnector: Option[ListeningConnector] = {
    virtualMachineManager.listeningConnectors().asScala
      .find(_.name() == ConnectorClassString)
  }
}
