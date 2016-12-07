package org.scaladebugger.tool.backend
import java.net.URI
import java.nio.file.Path

import ammonite.util.Bind
import org.scaladebugger.api.debuggers.Debugger
import org.scaladebugger.api.profiles.traits.info.{ThreadGroupInfo, ThreadInfo}
import org.scaladebugger.api.virtualmachines.{DummyScalaVirtualMachine, ScalaVirtualMachine}

case class State(
  activeDebugger: Option[Debugger],
  scalaVirtualMachines: Seq[ScalaVirtualMachine],
  dummyScalaVirtualMachine: DummyScalaVirtualMachine,
  activeThread: Option[ThreadInfo],
  activeThreadGroup: Option[ThreadGroupInfo],
  sourcePaths: Seq[Path],
  activeProfileName: String
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
    m :+= Bind("sourcePaths", sourcePaths)

    if (scalaVirtualMachines.nonEmpty) m :+= Bind("jvms", scalaVirtualMachines)
    else m :+= Bind("jvms", Seq(dummyScalaVirtualMachine))

    m
  }
}

object State {
  /**
   * Represents the default state where all values are None/Nil except the
   * dummy virtual machine, which is initialized using the default profile.
   */
  lazy val Default = newDefault()

  /**
   * Creates a new state with default values.
   *
   * @return The new state instance
   */
  def newDefault(): State = State(
    activeDebugger = None,
    scalaVirtualMachines = Nil,
    dummyScalaVirtualMachine = DummyScalaVirtualMachine.newInstance(),
    activeThread = None,
    activeThreadGroup = None,
    sourcePaths = Nil,
    activeProfileName = Debugger.DefaultProfileName
  )
}
