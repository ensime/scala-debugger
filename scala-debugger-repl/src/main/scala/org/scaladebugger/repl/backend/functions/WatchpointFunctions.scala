package org.scaladebugger.repl.backend.functions
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.repl.backend.StateManager

/**
 * Represents a collection of functions for managing watchpoints.
 *
 * @param stateManager The manager whose state to share among functions
 */
class WatchpointFunctions(private val stateManager: StateManager) {
  /** Entrypoint for watching access/modification of field in JVMs. */
  def watchAll(m: Map[String, Any]) = {
    handleWatchCommand(m, includeAccess = true, includeModification = true)
  }

  /** Entrypoint for watching access of field in JVMs. */
  def watchAccess(m: Map[String, Any]) = {
    handleWatchCommand(m, includeAccess = true, includeModification = false)
  }

  /** Entrypoint for watching modification of field in JVMs. */
  def watchModification(m: Map[String, Any]) = {
    handleWatchCommand(m, includeAccess = false, includeModification = true)
  }

  /** Entrypoint for listing all current watchpoints. */
  def listWatches(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    jvms.foreach(s => {
      println(s"<= ${s.uniqueId} =>")

      if (s.accessWatchpointRequests.nonEmpty) println("Access:")
      s.accessWatchpointRequests.zipWithIndex.foreach { case (awr, i) =>
        println(s"[${i+1}] ${awr.className}.${awr.fieldName}")
      }

      if (s.modificationWatchpointRequests.nonEmpty) println("Modification:")
      s.modificationWatchpointRequests.zipWithIndex.foreach { case (mwr, i) =>
        println(s"[${i+1}] ${mwr.className}.${mwr.fieldName}")
      }
    })
  }

  /** Entrypoint for unwatching access/modification of field in JVMs. */
  def unwatchAll(m: Map[String, Any]) = {
    handleUnwatchCommand(m, removeAccess = true, removeModification = true)
  }

  /** Entrypoint for unwatching access of field in JVMs. */
  def unwatchAccess(m: Map[String, Any]) = {
    handleUnwatchCommand(m, removeAccess = true, removeModification = false)
  }

  /** Entrypoint for unwatching modification of field in JVMs. */
  def unwatchModification(m: Map[String, Any]) = {
    handleUnwatchCommand(m, removeAccess = false, removeModification = true)
  }

  /**
   * Creates watch requests and outputs information.
   *
   * @param m The map of input to the command
   * @param includeAccess If true, adds an access watchpoint request
   * @param includeModification If true, adds a modification watchpoint request
   */
  private def handleWatchCommand(
    m: Map[String, Any],
    includeAccess: Boolean,
    includeModification: Boolean
  ) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val fieldName = m.get("field").map(_.toString).getOrElse(
      throw new RuntimeException("Missing field argument!")
    )

    jvms.foreach(s => {
      if (includeAccess) s.getOrCreateAccessWatchpointRequest(
        className, fieldName, NoResume
      ).foreach(e => {
        val loc = e.location()
        val sn = loc.sourceName()
        val ln = loc.lineNumber()
        println(s"$className.$fieldName accessed ($sn:$ln)")
      })

      if (includeModification) s.getOrCreateModificationWatchpointRequest(
        className, fieldName, NoResume
      ).foreach(e => {
        val loc = e.location()
        val sn = loc.sourceName()
        val ln = loc.lineNumber()
        println(s"$className.$fieldName modified ($sn:$ln)")
      })
    })
  }

  /**
   * Removes watch requests.
   *
   * @param m The map of input to the command
   * @param removeAccess If true, removes an access watchpoint request
   * @param removeModification If true, removes a modification watchpoint request
   */
  private def handleUnwatchCommand(
    m: Map[String, Any],
    removeAccess: Boolean,
    removeModification: Boolean
  ) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val fieldName = m.get("field").map(_.toString).getOrElse(
      throw new RuntimeException("Missing field argument!")
    )

    jvms.foreach(s => {
      if (removeAccess)
        s.removeAccessWatchpointRequests(className, fieldName)

      if (removeModification)
        s.removeModificationWatchpointRequests(className, fieldName)
    })
  }
}
