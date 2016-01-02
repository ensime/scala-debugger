package org.scaladebugger.repl

import org.parboiled2.ParseError
import org.scaladebugger.repl.frontend.DebuggerTerminal
import org.scaladebugger.repl.language.interpreters.DebuggerInterpreter
import org.scaladebugger.repl.language.parsers.DebuggerParser

import scala.util.{Try, Failure, Success}

object Main {

  def main(args: Array[String]): Unit = {
    val interpreter = new DebuggerInterpreter
    val terminal = new DebuggerTerminal

    //interpreter.repl()
    Try(terminal.run()) orElse Try(interpreter.repl())
  }

}
