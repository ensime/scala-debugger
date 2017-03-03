package org.scaladebugger.api.virtualmachines

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile

/**
 * Represents a virtual machine running Scala code.
 */
trait ScalaVirtualMachine extends SwappableDebugProfile with ProfileManager {
  /**
   * Initializes the ScalaVirtualMachine system.
   *
   * @param defaultProfile The default profile to use with the virtual machine
   * @param startProcessingEvents If true, immediately starts processing events
   */
  def initialize(
    defaultProfile: String = JavaDebugProfile.Name,
    startProcessingEvents: Boolean = true
  ): Unit

  /**
   * Starts actively processing events from the remote virtual machine.
   */
  def startProcessingEvents(): Unit

  /**
   * Stops actively processing events from the remote virtual machine.
   */
  def stopProcessingEvents(): Unit

  /**
   * Indicates whether or not events from the remote virtual machine are
   * actively being processed.
   *
   * @return True if being processed, otherwise false
   */
  def isProcessingEvents: Boolean

  /**
   * Indicates whether or not the virtual machine has been initialized.
   *
   * @return True if initialized, otherwise false
   */
  def isInitialized: Boolean

  /**
   * Indicates whether or not the virtual machine has started (received the
   * start event).
   *
   * @return True if started, otherwise false
   */
  def isStarted: Boolean

  /**
   * Represents the manager containing this virtual machine.
   */
  def manager: ScalaVirtualMachineManager

  /**
   * Represents the cache of objects available on the virtual machine.
   * Caching is done manually, so this cache is not populated as objects are
   * created on the virtual machine.
   */
  val cache: ObjectCache

  /**
   * Represents the collection of low-level APIs for the virtual machine.
   *
   * @return The container of low-level managers
   */
  val lowlevel: ManagerContainer

  /**
   * A unique id assigned to the Scala virtual machine on the client (library)
   * side to help distinguish multiple VMs.
   *
   * @return The unique id as a string
   */
  val uniqueId: String

  /**
   * Represents the underlying virtual machine represented by this Scala
   * virtual machine.
   *
   * @return The JDI VirtualMachine instance
   */
  val underlyingVirtualMachine: VirtualMachine

  /**
   * Resumes the virtual machine represented by the profile.
   */
  def resume(): Unit

  /**
   * Suspends the virtual machine represented by the profile.
   */
  def suspend(): Unit

  /**
   * Processes any pending requests contained by the provided Scala virtual
   * machine by applying them using this Scala virtual machine.
   *
   * @note This will not remove the pending requests from the managers
   *       contained in the provided Scala virtual machine!
   *
   * @param scalaVirtualMachine The virtual machine whose pending requests to
   *                            process using this virtual machine
   */
  def processPendingRequests(scalaVirtualMachine: ScalaVirtualMachine): Unit = {
    this.lowlevel.processPendingRequests(scalaVirtualMachine.lowlevel)
  }
}
