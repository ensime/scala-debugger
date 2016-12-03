package org.scaladebugger.api.virtualmachines
//import acyclic.file

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.{StandardProfileManager, ProfileManager}
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile

/**
 * Represents a virtual machine running Scala code whose operations do nothing.
 *
 * @param manager The manager containing this virtual machine
 * @param profileManager The manager used to provide specific implementations
 *                       of debugging via profiles
 * @param lowlevel The manager containing all low-level API managers
 */
class DummyScalaVirtualMachine private[virtualmachines] (
  override val manager: ScalaVirtualMachineManager,
  protected val profileManager: ProfileManager,
  override val lowlevel: ManagerContainer = ManagerContainer.usingDummyManagers()
) extends ScalaVirtualMachine {
  /**
   * Initializes the ScalaVirtualMachine system.
   *
   * @param defaultProfile The default profile to use with the virtual machine
   * @param startProcessingEvents If true, immediately starts processing events
   */
  override def initialize(
    defaultProfile: String,
    startProcessingEvents: Boolean
  ): Unit = use(defaultProfile)

  /**
   * Starts actively processing events from the remote virtual machine.
   */
  override def startProcessingEvents(): Unit = {}

  /**
   * Stops actively processing events from the remote virtual machine.
   */
  override def stopProcessingEvents(): Unit = {}

  /**
   * Indicates whether or not events from the remote virtual machine are
   * actively being processed.
   *
   * @return True if being processed, otherwise false
   */
  override def isProcessingEvents: Boolean = false

  /**
   * Indicates whether or not the virtual machine has been initialized.
   *
   * @return True if initialized, otherwise false
   */
  override def isInitialized: Boolean = false

  /**
   * Indicates whether or not the virtual machine has started (received the
   * start event).
   *
   * @return True if started, otherwise false
   */
  override def isStarted: Boolean = false

  /**
   * Represents the cache of objects available on the virtual machine. This
   * is unused by the dummy virtual machine.
   */
  override lazy val cache: ObjectCache = new ObjectCache

  /**
   * A unique id assigned to the Scala virtual machine on the client (library)
   * side to help distinguish multiple VMs.
   *
   * @return The unique id as a string
   */
  override val uniqueId: String = java.util.UUID.randomUUID().toString

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  override val underlyingVirtualMachine: VirtualMachine = null

  /**
   * Resumes the virtual machine represented by the profile.
   */
  override def resume(): Unit = {}

  /**
   * Suspends the virtual machine represented by the profile.
   */
  override def suspend(): Unit = {}

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

object DummyScalaVirtualMachine {
  /** Represents the default profile used by new instances of the dummy vm. */
  val DefaultProfileName = PureDebugProfile.Name

  /**
   * Creates a new instance of the dummy Scala virtual machine using a new
   * instance of the default profile manager.
   * Adds the dummy instance to the provided Scala virtual machine manager.
   *
   * @param scalaVirtualMachineManager The manager to tie to this dummy vm
   * @param defaultProfile The default profile to use with the dummy vm
   * @return The new dummy Scala virtual machine
   */
  def newInstance(
    scalaVirtualMachineManager: ScalaVirtualMachineManager,
    defaultProfile: String
  ): DummyScalaVirtualMachine = {
    val managerContainer = ManagerContainer.usingDummyManagers()
    val profileManager = new StandardProfileManager

    // Create the dummy Scala virtual machine and add it to the manager
    val dummyScalaVirtualMachine = scalaVirtualMachineManager.add(
      new DummyScalaVirtualMachine(
        scalaVirtualMachineManager,
        profileManager,
        managerContainer
      )
    )

    // Register default profiles
    StandardProfileManager.registerDefaultProfiles(
      profileManager,
      dummyScalaVirtualMachine,
      managerContainer
    )

    dummyScalaVirtualMachine.use(defaultProfile)

    dummyScalaVirtualMachine
  }

  /**
   * Creates a new instance of the dummy Scala virtual machine using a new
   * instance of the default profile manager. Uses the default profile.
   * Adds the dummy instance to the provided Scala virtual machine manager.
   *
   * @param scalaVirtualMachineManager The manager to tie to this dummy vm
   * @return The new dummy Scala virtual machine
   */
  def newInstance(
    scalaVirtualMachineManager: ScalaVirtualMachineManager
  ): DummyScalaVirtualMachine = newInstance(
    scalaVirtualMachineManager,
    DefaultProfileName
  )

  /**
   * Creates a new instance of the dummy Scala virtual machine using a new
   * instance of the default profile manager. Uses the default profile and
   * adds the dummy instance to the global Scala virtual machine manager.
   *
   * @return The new dummy Scala virtual machine
   */
  def newInstance(): DummyScalaVirtualMachine = newInstance(
    ScalaVirtualMachineManager.Instance
  )
}
