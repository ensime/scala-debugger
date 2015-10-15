package org.senkbeil.debugger.api

import java.util.concurrent.{Executors, ExecutorService}

import org.senkbeil.debugger.api.utils.LogLike
import com.sun.jdi.connect.{Connector, ListeningConnector}

import collection.JavaConverters._

import com.sun.jdi._

import scala.util.Try

object ListeningDebugger {
  /**
   * Creates a new instance of the listening debugger. Defaults to using a
   * single thread executor with one worker.
   *
   * @param virtualMachineManager The manager to use for virtual machine
   *                              connectors
   * @param address The address to use for remote JVMs to attach to this
   *                debugger
   * @param port The port to use for remote JVMs to attach to this debugger
   * @param executorServiceFunc The function used to create a new executor
   *                            service to use to spawn worker threads
   * @param workers The total number of worker tasks to spawn
   */
  def apply(
    address: String,
    port: Int,
    executorServiceFunc: () => ExecutorService =
      () => Executors.newSingleThreadExecutor(),
    workers: Int = 1
  )(
    implicit virtualMachineManager: VirtualMachineManager =
      Bootstrap.virtualMachineManager()
  ): ListeningDebugger = new ListeningDebugger(
    virtualMachineManager,
    address,
    port,
    executorServiceFunc,
    workers
  )
}

/**
 * Represents a debugger that listens for connections from remote JVMs.
 *
 * @param address The address to use for remote JVMs to attach to this debugger
 * @param port The port to use for remote JVMs to attach to this debugger
 * @param executorServiceFunc The function used to create a new executor
 *                            service to use to spawn worker threads
 * @param workers The total number of worker tasks to spawn
 *
 * @param virtualMachineManager The manager to use for virtual machine
 *                              connectors
 */
class ListeningDebugger private[debugger] (
  private val virtualMachineManager: VirtualMachineManager,
  private val address: String,
  private val port: Int,
  private val executorServiceFunc: () => ExecutorService,
  private val workers: Int
) extends Debugger with LogLike {
  private val ConnectorClassString = "com.sun.jdi.SocketListen"

  // Contains all components for the currently-running debugger
  @volatile private var components: Option[(
    ExecutorService, ListeningConnector, Map[String, _ <: Connector.Argument]
  )] = None

  /**
   * Represents the JVM options to feed to remote JVMs whom will connect to
   * this debugger.
   */
  val remoteJvmOptions = (
    s"-agentlib:jdwp=transport=dt_socket" ::
    s"server=n" ::
    s"suspend=n" ::
    s"address=$address:$port" ::
    Nil).mkString(",")

  /**
   * Indicates whether or not the debugger is running.
   *
   * @return True if it is running, otherwise false
   */
  def isRunning: Boolean = components.nonEmpty

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
   * @param newVirtualMachineFunc The function to be invoked once per JVM that
   *                              connects to this debugger
   * @tparam T The return type of the callback function
   */
  def start[T](newVirtualMachineFunc: VirtualMachine => T): Unit = {
    assert(!isRunning, "Debugger already started!")
    assertJdiLoaded()

    // Retrieve the listening connector, or throw an exception if failed
    val connector = findListeningConnector.getOrElse(
      throw new AssertionError("Unable to retrieve connector!"))

    val arguments = connector.defaultArguments()

    arguments.get("localAddress").setValue(address)
    arguments.get("port").setValue(port.toString)

    logger.info("Multiple Connections Allowed: " +
      connector.supportsMultipleConnections())

    // Open port for listening to JVM connections
    logger.info(s"Listening on $address:$port")
    connector.startListening(arguments)

    // Create the executor service to use
    logger.info("Creating executor service")
    val executorService = executorServiceFunc()

    // Store the connector and arguments (used for shutdown)
    components = Some((executorService, connector, arguments.asScala.toMap))

    // Start X workers to process connection requests
    logger.info(s"Spawning $workers worker tasks")
    (1 to workers).foreach(_ => executorService.execute(new Runnable {
      override def run(): Unit = while (!Thread.interrupted()) {
        val newVirtualMachine = Try(connector.accept(arguments))

        // Invoke our callback upon receiving a new virtual machine
        newVirtualMachine.foreach(newVirtualMachineFunc)

        // Release CPU
        Thread.sleep(1)
      }
    }))
  }

  def stop(): Unit = {
    assert(isRunning, "Debugger has not been started!")

    val (executorService, connector, arguments) = components.get

    // Close the listening port
    logger.info(s"Shutting down $address:$port")
    connector.stopListening(arguments.asJava)

    // Cancel all worker threads via interrupt
    logger.info("Cancelling worker threads")
    executorService.shutdownNow()

    // Mark that we have completely stopped the debugger
    components = None
  }

  /**
   * Retrieves the current listing of virtual machines that have connected to
   * this debugger.
   *
   * @return The collection of connected virtual machines
   */
  def connectedVirtualMachines =
    virtualMachineManager.connectedVirtualMachines().asScala.toSeq

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
