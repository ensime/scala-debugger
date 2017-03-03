package org.scaladebugger.api.virtualmachines

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.lowlevel.breakpoints.PendingBreakpointSupport
import org.scaladebugger.api.lowlevel.exceptions.PendingExceptionSupport
import org.scaladebugger.api.lowlevel.methods.{PendingMethodEntrySupport, PendingMethodExitSupport}
import org.scaladebugger.api.lowlevel.utils.JDIHelperMethods
import org.scaladebugger.api.lowlevel.watchpoints.{PendingAccessWatchpointSupport, PendingModificationWatchpointSupport}
import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.utils.{Logging, LoopingTaskRunner}
import com.sun.jdi._
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile

import scala.util.Try

/**
 * Represents a virtual machine running Scala code.
 *
 * @param manager The manager containing this virtual machine
 * @param _virtualMachine The underlying virtual machine
 * @param profileManager The manager used to provide specific implementations
 *                       of debugging via profiles
 * @param loopingTaskRunner The runner used to process events from remote JVMs
 * @param uniqueId A unique id assigned to the Scala virtual machine on the
 *                 client (library) side to help distinguish multiple VMs
 */
class StandardScalaVirtualMachine(
  override val manager: ScalaVirtualMachineManager,
  protected val _virtualMachine: VirtualMachine,
  protected val profileManager: ProfileManager,
  private val loopingTaskRunner: LoopingTaskRunner,
  override val uniqueId: String = java.util.UUID.randomUUID().toString
) extends ScalaVirtualMachine with JDIHelperMethods with Logging {
  private val initialized = new AtomicBoolean(false)
  private val started = new AtomicBoolean(false)
  protected lazy val eventManager = lowlevel.eventManager

  /**
   * Indicates whether or not the virtual machine has started (received the
   * start event).
   *
   * @return True if started, otherwise false
   */
  override def isStarted = started.get()

  /**
   * Indicates whether or not the virtual machine has been initialized.
   *
   * @return True if initialized, otherwise false
   */
  override def isInitialized: Boolean = initialized.get()

  /** Builds a string with the identifier of this virtual machine. */
  private def vmString(message: String) = s"(Scala VM $uniqueId) $message"

  /**
   * Creates a new instance of a manager container with newly-initialized
   * managers.
   *
   * @param loopingTaskRunner The looping task runner to provide to various
   *                          managers
   * @return The new container of managers
   */
  protected def newManagerContainer(
    loopingTaskRunner: LoopingTaskRunner
  ): ManagerContainer = ManagerContainer.fromVirtualMachine(
    virtualMachine = _virtualMachine,
    loopingTaskRunner = loopingTaskRunner,
    autoStartEventManager = false
  )

  /**
   * Represents the cache of objects available on the virtual machine.
   * Caching is done manually, so this cache is not populated as objects are
   * created on the virtual machine.
   */
  override lazy val cache: ObjectCache = new ObjectCache

  /** Represents the collection of low-level APIs for the virtual machine. */
  override lazy val lowlevel = newManagerContainer(loopingTaskRunner)

  /**
   * Initializes the ScalaVirtualMachine system.
   *
   * @param defaultProfile The default profile to use with the virtual machine
   * @param startProcessingEvents If true, immediately starts processing events
   */
  override def initialize(
    defaultProfile: String,
    startProcessingEvents: Boolean
  ): Unit = synchronized {
    assert(!isInitialized,
      s"Scala virtual machine '$uniqueId' already initialized!")

    logger.debug(vmString("Initializing Scala virtual machine!"))

    // Register our standard profiles
    registerStandardProfiles()

    // Mark our default profile
    this.use(defaultProfile)

    logger.debug(vmString("Adding custom event handlers!"))

    // Mark start event to load all of our system classes
    this.withProfile(defaultProfile).getOrCreateVMStartRequest().foreach(_ => {
      // Mark the VM as started
      started.set(true)

      logger.debug(vmString("Received start event!"))
      logger.trace(vmString("Refreshing all class references!"))
      lowlevel.classManager.refreshAllClasses()

      lowlevel.classManager.allFileNames.foreach(processPendingForFile)
      lowlevel.classManager.allClasses.map(_.name()).foreach(processPendingForClass)
    })

    // Mark class prepare events to signal refreshing our classes
    this.withProfile(defaultProfile).getOrCreateClassPrepareRequest().foreach(classPrepareEvent => {
      val referenceType = classPrepareEvent.referenceType
      val referenceTypeName = referenceType.name
      val fileName = lowlevel.classManager.fileNameForReferenceType(
        referenceType.toJdiInstance
      )

      // TODO: Extract these checks out to helper methods (perhaps in the
      // lowlevel class manager)
      if (
        referenceTypeName.startsWith("java") ||
        referenceTypeName.startsWith("sun") ||
        referenceTypeName.startsWith("scala")
      ) {
        logger.trace(vmString(s"Received new core class: $referenceTypeName"))
      } else {
        logger.trace(vmString(s"Received new non-core class: $referenceTypeName"))
      }
      lowlevel.classManager.refreshClass(referenceType.toJdiInstance)

      processPendingForFile(fileName)
      processPendingForClass(referenceTypeName)
    })

    initialized.set(true)

    // Try to start the event manager if indicated
    if (startProcessingEvents) this.startProcessingEvents()
  }

  /**
   * Starts actively processing events from the remote virtual machine.
   */
  override def startProcessingEvents(): Unit = synchronized {
    assert(isInitialized, "Scala virtual machine must be initialized!")

    // Ignore if already processing events
    if (isProcessingEvents) return

    // Process all pending requests immediately
    logger.debug("Processing all pending requests!")
    processOwnPendingRequests()

    logger.debug("Starting low-level event manager!")
    Try(eventManager.start())
  }

  protected def processOwnPendingRequests(): Unit =
    lowlevel.processPendingRequests()

  /**
   * Stops actively processing events from the remote virtual machine.
   */
  override def stopProcessingEvents(): Unit = Try(eventManager.stop())

  /**
   * Indicates whether or not events from the remote virtual machine are
   * actively being processed.
   *
   * @return True if being processed, otherwise false
   */
  override def isProcessingEvents: Boolean = eventManager.isRunning

  private def registerStandardProfiles(): Unit = {
    this.register(
      JavaDebugProfile.Name,
      new JavaDebugProfile(this, lowlevel)(_virtualMachine)
    )

    this.register(
      Scala210DebugProfile.Name,
      new Scala210DebugProfile(this, lowlevel)(_virtualMachine)
    )
  }

  private def processPendingForFile(fileName: String): Unit = {
    lowlevel.productIterator.foreach {
      case p: PendingBreakpointSupport  =>
        logger.trace(vmString(s"Processing any pending breakpoints for $fileName!"))
        p.processPendingBreakpointRequestsForFile(fileName)
      case _                            =>
    }
  }

  private def processPendingForClass(className: String): Unit = {
    lowlevel.productIterator.foreach {
      case p: PendingExceptionSupport               =>
        logger.trace(vmString(s"Processing any pending exceptions for $className!"))
        p.processPendingExceptionRequestsForClass(className)
      case p: PendingAccessWatchpointSupport        =>
        logger.trace(vmString(s"Processing any pending access watchpoints for $className!"))
        p.processPendingAccessWatchpointRequestsForClass(className)
      case p: PendingModificationWatchpointSupport  =>
        logger.trace(vmString(s"Processing any pending modification watchpoints for $className!"))
        p.processPendingModificationWatchpointRequestsForClass(className)
      case p: PendingMethodEntrySupport             =>
        logger.trace(vmString(s"Processing any pending method entries for $className!"))
        p.processPendingMethodEntryRequestsForClass(className)
      case p: PendingMethodExitSupport              =>
        logger.trace(vmString(s"Processing any pending method exits for $className!"))
        p.processPendingMethodExitRequestsForClass(className)
      case _                                        =>
    }
  }

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  override val underlyingVirtualMachine: VirtualMachine = _virtualMachine

  /**
   * Resumes the virtual machine represented by the profile.
   */
  override def resume(): Unit = underlyingVirtualMachine.resume()

  /**
   * Suspends the virtual machine represented by the profile.
   */
  override def suspend(): Unit = underlyingVirtualMachine.suspend()

  /**
   * Registers the profile using the provided name. Ignores any registration
   * under an already-used name.
   *
   * @param name The name of the profile to register
   * @param profile The profile to register
   */
  override def register(
    name: String,
    profile: DebugProfile
  ): Option[DebugProfile] = profileManager.register(name, profile)

  /**
   * Retrieves the profile with the provided name.
   *
   * @param name The name of the profile to retrieve
   * @return Some debug profile if found, otherwise None
   */
  override def retrieve(name: String): Option[DebugProfile] =
    profileManager.retrieve(name)

  /**
   * Unregisters the profile with the provided name.
   *
   * @param name The name of the profile to unregister
   * @return Some debug profile if unregistered, otherwise None
   */
  override def unregister(name: String): Option[DebugProfile] =
    profileManager.unregister(name)
}

