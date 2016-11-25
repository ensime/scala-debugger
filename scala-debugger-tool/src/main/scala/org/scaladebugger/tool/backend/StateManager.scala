package org.scaladebugger.tool.backend
import acyclic.file
import java.net.URI
import java.nio.file.Path

import com.sun.jdi.{ThreadGroupReference, ThreadReference}
import org.scaladebugger.api.debuggers.Debugger
import org.scaladebugger.api.profiles.traits.info.{ThreadGroupInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a manager for the backend state of the REPL.
 */
class StateManager {
  @volatile private var _state: State = State.newDefault()

  /**
   * Clears the state held by the manager, handling all appropriate shutdowns.
   */
  def clear() = {
    clearActiveDebugger()
    updateState(State.newDefault())
  }

  /**
   * Returns the current state held by the manager.
   *
   * @return The current state
   */
  def state: State = _state

  /**
   * Updates the entire state contained by this manager.
   *
   * @param newState The new state held by this manager
   */
  def updateState(newState: State) = {
    require(newState != null)
    _state = newState
  }

  /**
   * Updates the active debugger held by the state in this manager.
   *
   * @param debugger The debugger to serve as the active debugger
   */
  def updateActiveDebugger(debugger: Debugger) = {
    updateState(state.copy(activeDebugger = Some(debugger)))
  }

  /**
   * Clears the active debugger, invoking its shutdown procedure.
   */
  def clearActiveDebugger() = {
    state.activeDebugger.foreach(_.stop())
    updateState(state.copy(activeDebugger = None))
  }

  /**
   * Updates the collection of Scala virtual machines held by the state in this
   * manager. Syncs their profiles to the current active profile.
   *
   * @param scalaVirtualMachines The new collection of Scala virtual machines
   */
  def updateScalaVirtualMachines(scalaVirtualMachines: Seq[ScalaVirtualMachine]) = {
    // Sync profiles
    scalaVirtualMachines.foreach(_.use(state.activeProfileName))

    // Update virtual machines
    updateState(state.copy(scalaVirtualMachines = scalaVirtualMachines))
  }

  /**
   * Adds a new Scala virtual machine to the collection held by the state in
   * this manager.
   *
   * @param scalaVirtualMachine The new Scala virtual machine to add
   */
  def addScalaVirtualMachine(scalaVirtualMachine: ScalaVirtualMachine) = {
    val scalaVirtualMachines = state.scalaVirtualMachines :+ scalaVirtualMachine
    updateScalaVirtualMachines(scalaVirtualMachines)
  }

  /**
   * Clears the collection of Scala virtual machines.
   */
  def clearScalaVirtualMachines() = updateScalaVirtualMachines(Nil)

  /**
   * Updates the active thread held by the state in this manager.
   *
   * @param thread The thread to serve as the active thread
   */
  def updateActiveThread(thread: ThreadInfoProfile) = {
    updateState(state.copy(activeThread = Some(thread)))
  }

  /**
   * Clears the active thread.
   */
  def clearActiveThread() = updateState(state.copy(activeThread = None))

  /**
   * Updates the active thread group held by the state in this manager.
   *
   * @param threadGroup The thread group to serve as the active thread group
   */
  def updateActiveThreadGroup(threadGroup: ThreadGroupInfoProfile) = {
    updateState(state.copy(activeThreadGroup = Some(threadGroup)))
  }

  /**
   * Clears the active thread group.
   */
  def clearActiveThreadGroup() =
    updateState(state.copy(activeThreadGroup = None))

  /**
   * Updates the collection of source paths held by the state in this manager.
   *
   * @param sourcePaths The new collection of source paths
   */
  def updateSourcePaths(sourcePaths: Seq[Path]) = {
    updateState(state.copy(sourcePaths = sourcePaths))
  }

  /**
   * Adds a new source path to the collection held by the state in this manager.
   *
   * @param sourcePath The new Scala virtual machine to add
   */
  def addSourcePath(sourcePath: Path) = {
    val sourcePaths = state.sourcePaths :+ sourcePath
    updateSourcePaths(sourcePaths)
  }

  /**
   * Clears the collection of source paths.
   */
  def clearSourcePaths() = updateState(state.copy(sourcePaths = Nil))

  /**
   * Updates the active profile for the dummy and active virtual machines.
   *
   * @param name The name of the new profile
   */
  def updateActiveProfile(name: String) = {
    // Sync all virtual machines
    state.scalaVirtualMachines.foreach(_.use(name))
    state.dummyScalaVirtualMachine.use(name)

    // Sync active thread
    state.activeThread.foreach(t => {
      // Scala virtual machine's profile has been updated, so this
      // should generate a thread under the correct profile
      val at = t.scalaVirtualMachine.tryThread(t.uniqueId)
      at.foreach(updateActiveThread)
      at.failed.foreach(_ => clearActiveThread())
    })

    // Sync active thread group
    state.activeThreadGroup.foreach(tg => {
      // Scala virtual machine's profile has been updated, so this
      // should generate a thread group under the correct profile
      val atg = tg.scalaVirtualMachine.tryThreadGroup(tg.uniqueId)
      atg.foreach(updateActiveThreadGroup)
      atg.failed.foreach(_ => clearActiveThreadGroup())
    })

    // Update profile name
    updateState(state.copy(activeProfileName = name))
  }
}
