package org.scaladebugger.repl.language.models

import org.scaladebugger.api.debuggers.Debugger
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

case class State(
  activeDebugger: Option[Debugger],
  scalaVirtualMachines: Seq[ScalaVirtualMachine]
)
