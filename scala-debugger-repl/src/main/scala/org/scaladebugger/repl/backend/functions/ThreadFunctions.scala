package org.scaladebugger.repl.backend.functions
import acyclic.file
import org.scaladebugger.api.profiles.traits.info.ThreadInfoProfile
import org.scaladebugger.repl.backend.StateManager

/**
 * Represents a collection of functions for managing threads.
 *
 * @param stateManager The manager whose state to share among functions
 */
class ThreadFunctions(private val stateManager: StateManager) {

  /** Entrypoint for listing thread information for connected JVMs. */
  def threads(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    // Optional thread group (argument has priority, followed by active)
    val threadGroup = m.get("threadGroup").map(_.toString)
      .orElse(stateManager.state.activeThreadGroup.map(_.name))

    jvms.foreach(s => {
      val threadGroups = s.threads
        .map(_.threadGroup)
        .groupBy(_.name)
        .map(_._2.head)
        .filter(tg => threadGroup.isEmpty || tg.name == threadGroup.get)

      println(s"<= JVM ${s.uniqueId} =>")
      threadGroups.foreach(tg => {
        println("Group " + tg.name + ":")
        tg.threads.foreach(t => {
          val rName = t.referenceType.name
          val id = "0x" + t.uniqueIdHexString
          val name = t.name
          val status = t.status.statusString
          val suspended = if (t.status.isSuspended) "suspended" else ""
          val atBreakpoint = if (t.status.isAtBreakpoint) "at breakpoint" else ""
          println(s"\t($rName)$id $name $status $suspended $atBreakpoint")
        })
      })
    })
  }

  /** Entrypoint for setting the default thread. */
  def thread(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val threadName = m.get("thread").map(_.toString)

    threadName match {
      // If name provided, lookup and set as active thread
      case Some(name) =>
        val thread = jvms.view.flatMap(_.threads).collectFirst {
          case t if t.name == name => t
        }

        if (thread.isEmpty) println(s"No thread found named '$name'!")
        thread.foreach(stateManager.updateActiveThread)

      // No name provided, so clear existing active thread
      case None =>
        stateManager.clearActiveThread()
    }
  }

  /** Entrypoint for suspending one or more threads or the entire JVM. */
  def suspend(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    // TODO: Support more than one thread
    val threadNames = m.get("thread").map(t => Seq(t.toString)).getOrElse(Nil)

    // If specified thread(s), suspend those specific threads
    if (threadNames.nonEmpty) {
      val threads = jvms.flatMap(_.threads)
        .filter(t => threadNames.contains(t.name))

      threads.foreach(_.suspend())

    // Suspend entire JVM
    } else {
      jvms.foreach(_.suspend())
    }
  }

  /** Entrypoint for resuming one or more threads or the entire JVM. */
  def resume(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    // TODO: Support more than one thread
    val threadNames = m.get("thread").map(t => Seq(t.toString)).getOrElse(Nil)

    // If specified thread(s), resume those specific threads
    if (threadNames.nonEmpty) {
      val threads = jvms.flatMap(_.threads)
        .filter(t => threadNames.contains(t.name))

      threads.foreach(_.resume())

    // Resume entire JVM
    } else {
      jvms.foreach(_.resume())
    }
  }

  /** Entrypoint for dumping the stack for one or more threads. */
  def where(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    // TODO: Support more than one thread
    val threadNames = m.get("thread").map(t => Seq(t.toString)).getOrElse(Nil)

    // If specified thread(s), dump stack for each
    if (threadNames.nonEmpty) {
      val threads = jvms.flatMap(_.threads)
        .filter(t => threadNames.contains(t.name))

      threads.foreach(printFrameStack)

    // Dump stack for current thread
    } else {
      stateManager.state.activeThread match {
        case Some(t) => printFrameStack(t)
        case None => println("No active thread!")
      }
    }
  }

  private def printFrameStack(threadInfo: ThreadInfoProfile) = {
    threadInfo.suspend()

    val f = threadInfo.frames.map(_.location).zipWithIndex.map { case (l, i) =>
      s"[${i+1}] " +
      s"${l.declaringType.name}.${l.method.name} " +
      s"(${l.sourceName}:${l.lineNumber})"
    }.mkString("\n")

    println(f)

    threadInfo.resume()
  }
}
