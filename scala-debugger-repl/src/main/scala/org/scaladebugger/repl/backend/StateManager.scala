package org.scaladebugger.repl.backend
import acyclic.file
import java.net.URI

import com.sun.jdi.{ThreadGroupReference, ThreadReference}
import org.scaladebugger.api.debuggers.Debugger
import org.scaladebugger.api.profiles.traits.info.{ThreadGroupInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents a manager for the backend state of the REPL.
 */
class StateManager {
  @volatile private var _state: State = State.Default

  /**
   * Clears the state held by the manager, handling all appropriate shutdowns.
   */
  def clear() = {
    clearActiveDebugger()
    updateState(State.Default)
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
   * manager.
   *
   * @param scalaVirtualMachines The new collection of Scala virtual machines
   */
  def updateScalaVirtualMachines(scalaVirtualMachines: Seq[ScalaVirtualMachine]) = {
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
  def updateSourcePaths(sourcePaths: Seq[URI]) = {
    updateState(state.copy(sourcePaths = sourcePaths))
  }

  /**
   * Adds a new source path to the collection held by the state in this manager.
   *
   * @param sourcePath The new Scala virtual machine to add
   */
  def addSourcePath(sourcePath: URI) = {
    val sourcePaths = state.sourcePaths :+ sourcePath
    updateSourcePaths(sourcePaths)
  }

  /**
   * Clears the collection of source paths.
   */
  def clearSourcePaths() = updateState(state.copy(sourcePaths = Nil))
}
