package org.scaladebugger.repl.frontend
import acyclic.file

import org.parboiled2.ParseError
import org.scaladebugger.language.interpreters.Interpreter
import org.scaladebugger.repl.backend.StateManager

import scala.annotation.tailrec
import scala.util.{Try, Failure, Success}

import org.scaladebugger.language.models.Undefined

/**
 * Represents a REPL available through the terminal.
 *
 * @param interpreter The interpreter to power the REPL
 * @param mainTerminal The main terminal to use with the REPL
 * @param forceUseFallback If true, skips the fancy terminal and uses the
 *                         fallback implementation instead
 * @param printUndefined If true, prints out undefined when received from
 *                       the interpreter as a result
 */
class Repl (
  private val interpreter: Interpreter,
  private val stateManager: StateManager,
  private val mainTerminal: Terminal = new FancyTerminal,
  private val forceUseFallback: Boolean = false,
  private val printUndefined: Boolean = false
) {
  /**
   * Starts the REPL.
   */
  def start(): Unit = {
    val result = Try(if (!forceUseFallback) mainImpl() else {})

    result.failed.foreach { _ =>
      Console.err.println("Main terminal failed! Trying fallback!")
    }

    if (result.isFailure || forceUseFallback) {
      Console.out.println("Fallback REPL starting! Assuming 80 character width!")
      fallbackImpl()
    }
  }

  /**
   * Returns a new REPL using the provided terminal.
   *
   * @param terminal The terminal to use as the frontend of the REPL
   * @return The new REPL instance
   */
  def withTerminal(terminal: Terminal): Repl = new Repl(
    interpreter = interpreter,
    stateManager = stateManager,
    mainTerminal = terminal
  )

  /**
   * Returns the active terminal instance used with this REPL.
   *
   * @return The terminal instance
   */
  def activeTerminal: Terminal = mainTerminal

  /**
   * Represents the feature-rich implementation of the terminal.
   */
  private def mainImpl(): Unit = next(mainTerminal)

  /**
   * Represents the fallback implementation of the terminal when unable to use
   * the more feature-rich terminal. Inherits prompt function from active
   * terminal.
   */
  private def fallbackImpl(): Unit = next({
    val ft = new FallbackTerminal
    ft.setPromptFunction(activeTerminal.getPromptFunction)
    ft
  })

  /**
   * Retrieves input from the provided terminal, evaluates, and continues.
   *
   * @param terminal The terminal whose input to retrieve
   */
  @tailrec private def next(terminal: Terminal): Unit = {
    val continue = terminal.readLine().exists(processText)
    if (continue) next(terminal)
  }

  /**
   * Processes the input text of the REPL, performing any actions.
   *
   * @param text The input text to process
   *
   * @return True if the REPL should continue, otherwise false
   */
  private def processText(text: String): Boolean = text match {
    case "" =>
      true
    case "inline" =>
      val b = stateManager.state.toBindings
      Try(ammonite.repl.Main.debug(b: _*)).failed.foreach(Console.err.println)
      true
    case "quit" =>
      println("Exiting")
      false
    case line if TerminalUtilities.isHelpRequest(line) =>
      TerminalUtilities.printHelp(interpreter.context, line)
      true
    case line if !TerminalUtilities.isHelpRequest(line) =>
      interpreter.interpret(line) match {
        case Success(Undefined.Value) =>
          if (printUndefined) println(Undefined.toScalaValue)
        case Success(v)               => println(v)
        case Failure(ex: ParseError)  => println(ex.format(line))
        case Failure(ex)              => println(ex)
      }
      true
  }

}
