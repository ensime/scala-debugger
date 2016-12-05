package org.scaladebugger.api.virtualmachines

import java.util.concurrent.{ConcurrentHashMap, ConcurrentLinkedQueue}

import com.sun.jdi.VirtualMachine

import scala.collection.JavaConverters._

object ScalaVirtualMachineManager {
  /** Represents the global Scala virtual machine manager. */
  val Instance = new ScalaVirtualMachineManager
}

/**
 * Represents a manager of virtual machines, providing a variety of means to
 * look up a virtual machine.
 */
class ScalaVirtualMachineManager private[api] extends Traversable[ScalaVirtualMachine] {
  type VM = VirtualMachine
  type SVM = ScalaVirtualMachine
  private type IDM = scala.collection.mutable.Map[String, VM]
  private type VMM = scala.collection.mutable.Map[VM, SVM]
  private type VMB = scala.collection.mutable.Buffer[SVM]

  /** Contains a mapping of unique ids to their respective virtual machines. */
  private val uniqueIdMap: IDM = new ConcurrentHashMap[String, VM]().asScala

  /** Contains a mapping of JDI VMs to the Scala VMs. */
  private val vmMap: VMM = new ConcurrentHashMap[VM, SVM]().asScala

  /** Contains the buffer of Scala virtual machines. */
  private val scalaVirtualMachines: VMB = scala.collection.mutable.Buffer()

  /**
   * Executes the provided code with all internal structures synchronized.
   *
   * @param thunk The code to execute
   * @tparam T The type of the return value of the code
   * @return The result of the executed code
   */
  private def sync[T](thunk: => T): T = {
    uniqueIdMap.synchronized {
      vmMap.synchronized {
        scalaVirtualMachines.synchronized {
          thunk
        }
      }
    }
  }

  /**
   * Adds a new Scala virtual machine to be managed. Ignores SVMs already
   * added to this manager.
   *
   * @param scalaVirtualMachine The new Scala virtual machine instance
   * @return The added Scala virtual machine
   * @throws IllegalArgumentException When the Scala virtual machine is null
   */
  @throws[IllegalArgumentException]
  def add[T <: SVM](scalaVirtualMachine: T): T = {
    require(scalaVirtualMachine != null,
      "Scala virtual machine cannot be null!")

    sync {
      val hasSVM = scalaVirtualMachines.contains(scalaVirtualMachine)

      if (!hasSVM) {
        scalaVirtualMachines += scalaVirtualMachine
        Option(scalaVirtualMachine.underlyingVirtualMachine).foreach { vm =>
          vmMap.put(vm, scalaVirtualMachine)
          Option(scalaVirtualMachine.uniqueId).foreach { id =>
            uniqueIdMap.put(id, vm)
          }
        }
      }
    }

    scalaVirtualMachine
  }

  /**
   * Removes the Scala virtual machine from the manager.
   *
   * @param scalaVirtualMachine The Scala virtual machine to remove
   * @return Some Scala virtual machine representing the removed virtual machine
   *         if found and removed, otherwise None
   * @throws IllegalArgumentException When the Scala virtual machine is null
   */
  @throws[IllegalArgumentException]
  def remove[T <: SVM](scalaVirtualMachine: T): Option[T] = {
    require(scalaVirtualMachine != null,
      "Scala virtual machine cannot be null!")

    sync({
      val hasSVM = scalaVirtualMachines.contains(scalaVirtualMachine)

      if (hasSVM) {
        scalaVirtualMachines -= scalaVirtualMachine
        Option(scalaVirtualMachine.underlyingVirtualMachine).foreach { vm =>
          vmMap.remove(vm)
          Option(scalaVirtualMachine.uniqueId).foreach { id =>
            uniqueIdMap.remove(id)
          }
        }
      }

      if (hasSVM) Some(scalaVirtualMachine) else None
    })
  }

  /**
   * Clears the manager of all Scala virtual machine references.
   */
  def clear(): Unit = sync {
    uniqueIdMap.clear()
    vmMap.clear()
    scalaVirtualMachines.clear()
  }

  /**
   * Iterates over all Scala virtual machine instances, executing the provided
   * function.
   *
   * @param f The function to execute on each Scala virtual machine
   * @tparam U The return type of the function being executed
   */
  override def foreach[U](f: (SVM) => U): Unit = scalaVirtualMachines.foreach(f)

  /**
   * Looks up a Scala virtual machine by its unique id.
   *
   * @param uniqueId The unique id of the Scala virtual machine to retrieve
   * @return The associated Scala virtual machine
   * @throws IllegalStateException When no Scala virtual machine found
   */
  @throws[IllegalStateException]
  def apply(uniqueId: String): SVM = {
    get(uniqueId).getOrElse(throw new IllegalStateException(
      s"No Scala virtual machine found with unique id $uniqueId"
    ))
  }

  /**
   * Looks up a Scala virtual machine by its unique id.
   *
   * @param uniqueId The unique id of the Scala virtual machine to retrieve
   * @return Some Scala virtual machine if found, otherwise None
   */
  def get(uniqueId: String): Option[SVM] = {
    Option(uniqueId).flatMap(uniqueIdMap.get).flatMap(get)
  }

  /**
   * Looks up a Scala virtual machine by its virtual machine.
   *
   * @param virtualMachine The low-level virtual machine that the Scala virtual
   *                       machine wraps
   * @return The associated Scala virtual machine
   * @throws IllegalStateException When no Scala virtual machine found
   */
  @throws[IllegalStateException]
  def apply(virtualMachine: VM): SVM = {
    get(virtualMachine).getOrElse(throw new IllegalStateException(
      s"No Scala virtual machine found with virtual machine $virtualMachine"
    ))
  }

  /**
   * Looks up a Scala virtual machine by its virtual machine.
   *
   * @param virtualMachine The low-level virtual machine that the Scala virtual
   *                       machine wraps
   * @return Some Scala virtual machine if found, otherwise None
   */
  def get(virtualMachine: VM): Option[SVM] = {
    Option(virtualMachine).flatMap(vmMap.get)
  }
}
