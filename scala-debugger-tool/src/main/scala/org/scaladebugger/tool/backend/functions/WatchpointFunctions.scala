package org.scaladebugger.tool.backend.functions
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.lowlevel.watchpoints.{AccessWatchpointRequestInfo, ModificationWatchpointRequestInfo}
import org.scaladebugger.tool.backend.StateManager

/**
 * Represents a collection of functions for managing watchpoints.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class WatchpointFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
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
    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    def pstr(p: Boolean): String = if (p) "Pending" else "Active"

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")

      val aMap = s.accessWatchpointRequests.groupBy(_.className)
      val mMap = s.modificationWatchpointRequests.groupBy(_.className)
      val keys = aMap.keySet ++ mMap.keySet

      // For each class, group the fields
      keys.foreach(k => {
        val className = k
        val fieldRequests = aMap.getOrElse(k, Nil) ++ mMap.getOrElse(k, Nil)
        val fMap = fieldRequests.groupBy {
          case r: AccessWatchpointRequestInfo => r.fieldName
          case r: ModificationWatchpointRequestInfo => r.fieldName
        }

        // For each field, print data
        writeLine(s"{Class $className}")
        fMap.keySet.foreach(k => {
          val fieldName = k
          val awr = fMap(k).find(_.isInstanceOf[AccessWatchpointRequestInfo])
          val mwr = fMap(k).find(_.isInstanceOf[ModificationWatchpointRequestInfo])
          val ap = awr.map(r =>
            "[Access: " + (if (r.isPending) "Pending" else "Active") + "]"
          ).getOrElse("")
          val mp = mwr.map(r =>
            "[Modification: " + (if (r.isPending) "Pending" else "Active") + "]"
          ).getOrElse("")

          val line = s"-> Field '$fieldName' $ap $mp".trim()
          writeLine(line)
        })
      })
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
    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val fieldName = m.get("field").map(_.toString).getOrElse(
      throw new RuntimeException("Missing field argument!")
    )

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    val text =
      (if (includeAccess) "access" else "") +
      (if (includeAccess && includeModification) " and " else "") +
      (if (includeModification) "modification" else "")
    writeLine(s"Watching '$fieldName' of '$className' for " + text)

    jvms.foreach(s => {
      if (includeAccess) s.getOrCreateAccessWatchpointRequest(
        className, fieldName, NoResume
      ).foreach(e => {
        val loc = e.location()
        val sn = loc.sourceName()
        val ln = loc.lineNumber()
        writeLine(s"'$fieldName' of '$className' accessed ($sn:$ln)")
      })

      if (includeModification) s.getOrCreateModificationWatchpointRequest(
        className, fieldName, NoResume
      ).foreach(e => {
        val loc = e.location()
        val sn = loc.sourceName()
        val ln = loc.lineNumber()
        writeLine(s"'$fieldName' of '$className' modified ($sn:$ln)")
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
    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val fieldName = m.get("field").map(_.toString).getOrElse(
      throw new RuntimeException("Missing field argument!")
    )

    // Set as pending if no JVM is available
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      jvms = Seq(stateManager.state.dummyScalaVirtualMachine)
    }

    jvms.foreach(s => {
      if (removeAccess)
        s.removeAccessWatchpointRequests(className, fieldName)

      if (removeModification)
        s.removeModificationWatchpointRequests(className, fieldName)
    })
  }
}
