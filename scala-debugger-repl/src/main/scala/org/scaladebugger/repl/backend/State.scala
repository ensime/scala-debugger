package org.scaladebugger.repl.backend
import acyclic.file
import java.io.File
import java.net.URI

import ammonite.util.Bind
import com.sun.jdi.{ThreadGroupReference, ThreadReference}
import org.scaladebugger.api.debuggers.Debugger
import org.scaladebugger.api.profiles.traits.info.{ThreadGroupInfoProfile, ThreadInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

case class State(
  activeDebugger: Option[Debugger],
  scalaVirtualMachines: Seq[ScalaVirtualMachine],
  activeThread: Option[ThreadInfoProfile],
  activeThreadGroup: Option[ThreadGroupInfoProfile],
  sourcePaths: Seq[URI]
) {
  /**
   * Converts this object to a collection of bindings that only contains
   * fields that are not empty.
   *
   * @return The state as a collection of bindings
   */
  def toBindings: Seq[Bind[_]] = {
    var m: Seq[Bind[_]] = Nil

    activeDebugger.foreach(d => m :+= Bind("debugger", d))
    activeThread.foreach(t => m :+= Bind("thread", t))
    activeThreadGroup.foreach(tg => m :+= Bind("threadGroup", tg))
    if (scalaVirtualMachines.nonEmpty) m :+= Bind("jvms", scalaVirtualMachines)

    m
  }
}

object State {
  /** Represents the default state (all values are None/Nil). */
  val Default = State(
    activeDebugger = None,
    scalaVirtualMachines = Nil,
    activeThread = None,
    activeThreadGroup = None,
    sourcePaths = Nil
  )
}
