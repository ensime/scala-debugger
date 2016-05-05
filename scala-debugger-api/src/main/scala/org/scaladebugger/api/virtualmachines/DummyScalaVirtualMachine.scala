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
 * @param profileManager The manager used to provide specific implementations
 *                       of debugging via profiles
 */
class DummyScalaVirtualMachine(
  protected val profileManager: ProfileManager,
  override val lowlevel: ManagerContainer = ManagerContainer.usingDummyManagers()
) extends ScalaVirtualMachine {
  /**
   * Initializes the ScalaVirtualMachine system.
   *
   * @param startProcessingEvents If true, immediately starts processing events
   */
  override def initialize(startProcessingEvents: Boolean = true): Unit = {}

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
  /**
   * Creates a new instance of the dummy Scala virtual machine using a new
   * instance of the default profile manager.
   *
   * @return The new dummy Scala virtual machine
   */
  def newInstance(): DummyScalaVirtualMachine = {
    val managerContainer = ManagerContainer.usingDummyManagers()

    val dummyScalaVirtualMachine = new DummyScalaVirtualMachine(
      StandardProfileManager.newDefaultInstance(managerContainer),
      managerContainer
    )

    dummyScalaVirtualMachine.use(PureDebugProfile.Name)

    dummyScalaVirtualMachine
  }
}
