package org.scaladebugger.tool

import java.io.File

import org.parboiled2.ParseError
import org.scaladebugger.language.interpreters.{DebuggerInterpreter, Interpreter}
import org.scaladebugger.language.models.Undefined
import org.scaladebugger.tool.backend.StateManager
import org.scaladebugger.tool.frontend.completion.CompletionContext
import org.scaladebugger.tool.frontend.history.FileHistoryManager
import org.scaladebugger.tool.frontend.{FallbackTerminal, FancyTerminal, Terminal, TerminalUtilities}

import scala.annotation.tailrec
import scala.util.{Failure, Success, Try}

/**
 * Represents a REPL available through the terminal.
 *
 * @param interpreter The interpreter to power the REPL
 * @param stateManager The manager for the state of the REPL
 * @param config The configuration associated with the REPL from the CLI
 * @param newTerminal Function to create a new terminal for use with the REPL
 */
class Repl (
  val interpreter: Interpreter,
  val stateManager: StateManager,
  val config: Config = new Config(Nil),
  private val newTerminal: (Config, CompletionContext) => Terminal =
    Repl.defaultNewTerminal
) {
  private val completionContext = CompletionContext.fromLanguageContext(
    interpreter.context
  )
  private val mainTerminal: Terminal = newTerminal(config, completionContext)

  /** Main execution thread of the REPL. */
  private val executionThread = new Thread(new Runnable {
    override def run(): Unit = {
      try {
        val result = Try(if (!config.forceUseFallback()) mainImpl() else {})

        result.failed.foreach { _ =>
          Console.err.println("Main terminal failed! Trying fallback!")
        }

        if (result.isFailure || config.forceUseFallback()) {
          Console.out.println("Fallback REPL starting! Assuming 80 character width!")
          fallbackImpl()
        }
      } catch {
        case _: InterruptedException => /*
          Suppress throwing of interrupt exceptions (but still end execution).
        */
      } finally {
        stateManager.clear()
      }
    }
  })

  /**
   * Starts the REPL. Asynchronous operation that does not block the thread
   * executing this method.
   */
  def start(): Unit = {
    if (!executionThread.isAlive) executionThread.start()
  }

  /**
   * Stops the REPL. Blocks until fully stopped.
   */
  def stop(): Unit = {
    if (executionThread.isAlive) {
      executionThread.interrupt()
      executionThread.join()
    }
  }

  /**
   * Returns whether or not the REPL is running.
   *
   * @return True if running (started and active), otherwise false
   */
  def isRunning: Boolean = executionThread.isAlive

  /**
   * Returns a new REPL using the provided terminal.
   *
   * @param terminal The terminal to use as the frontend of the REPL
   * @return The new REPL instance
   */
  def withTerminal(terminal: Terminal): Repl = new Repl(
    interpreter = interpreter,
    stateManager = stateManager,
    config = config,
    newTerminal = (_, _) => terminal
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
      Try(ammonite.Main().run(b: _*)).failed.foreach(Console.err.println)
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
          if (config.printUndefined()) println(Undefined.toScalaValue)
        case Success(v)               => println(v)
        case Failure(ex: ParseError)  => println(ex.format(line))
        case Failure(ex)              => println(ex)
      }
      true
  }
}

object Repl {
  /** Represents the default method to create a new main terminal. */
  lazy val defaultNewTerminal: (Config, CompletionContext) => Terminal = {
    (config: Config, completionContext: CompletionContext) => {
      val historyUri = new File(config.historyFile()).toURI
      FileHistoryManager.newInstance(historyUri, config.historyMaxLines()) match {
        case Success(historyManager) =>
          new FancyTerminal(historyManager, completionContext)
        case Failure(throwable) =>
          throw throwable
      }
    }
  }

  /**
   * Creates a new instance of the REPL with custom functions.
   *
   * @param config The configuration associated with the REPL from the CLI
   * @param newTerminal Function to create a new terminal for use with the REPL
   *
   * @return The new REPL instance
   */
  def newInstance(
    config: Config = new Config(Nil),
    newTerminal: (Config, CompletionContext) => Terminal = Repl.defaultNewTerminal
  ): Repl = {
    import org.scaladebugger.tool.backend.functions._
    val interpreter = new DebuggerInterpreter
    val stateManager = new StateManager
    stateManager.updateActiveProfile(config.defaultProfile())

    val repl = new Repl(
      interpreter   = interpreter,
      stateManager  = stateManager,
      config        = config,
      newTerminal   = newTerminal
    )

    val writeLine = (text: String) => repl.activeTerminal.writeLine(text)
    val debuggerFunctions = new DebuggerFunctions(stateManager, writeLine)
    val threadFunctions = new ThreadFunctions(stateManager, writeLine)
    val expressionFunctions = new ExpressionFunctions(stateManager, writeLine)
    val breakpointFunctions = new BreakpointFunctions(stateManager, writeLine)
    val methodFunctions = new MethodFunctions(stateManager, writeLine)
    val classFunctions = new ClassFunctions(stateManager, writeLine)
    val threadGroupFunctions = new ThreadGroupFunctions(stateManager, writeLine)
    val sourceFunctions = new SourceFunctions(stateManager, writeLine)
    val stepFunctions = new StepFunctions(stateManager, writeLine)
    val exceptionFunctions = new ExceptionFunctions(stateManager, writeLine)
    val watchpointFunctions = new WatchpointFunctions(stateManager, writeLine)

    interpreter.bindFunction("attach", Seq("port", "hostname", "timeout"), debuggerFunctions.attach, "Attaches to an already-running JVM process using a port.")
    interpreter.bindFunction("attachp", Seq("pid", "timeout"), debuggerFunctions.attachp, "Attaches to an already-running JVM process using its pid.")
    interpreter.bindFunction("launch", Seq("class", "suspend"), debuggerFunctions.launch, "Launches a new JVM process and attaches to it.")
    interpreter.bindFunction("listen", Seq("port", "hostname"), debuggerFunctions.listen, "Listens for incoming JVM connections.")
    interpreter.bindFunction("profile", Seq("name"), debuggerFunctions.profile, "Sets the profile to the provided name or prints out the active profile if no name specified.")
    interpreter.bindFunction("profiles", Nil, debuggerFunctions.profiles, "Displays all available profile names.")
    interpreter.bindFunction("stop", Nil, debuggerFunctions.stop, "Stops the current debugger and resets REPL state.")

    interpreter.bindFunction("threads", Seq("threadGroup"), threadFunctions.threads, "Lists threads, optionally for a specific thread group.")
    interpreter.bindFunction("thread", Seq("thread"), threadFunctions.thread, "Sets the active thread with the specified id. No id clears the active thread.")
    interpreter.bindFunction("suspend", Seq("thread"), threadFunctions.suspend, "Suspends the specified threads or the entire JVM.")
    interpreter.bindFunction("resume", Seq("thread"), threadFunctions.resume, "Resumes the specified threads or the entire JVM.")
    interpreter.bindFunction("where", Seq("thread"), threadFunctions.where, "Dumps the stack of the specified threads or the active thread.")

    interpreter.bindFunction("examine", Seq("expression"), expressionFunctions.examine, "Examines and prints the provided expression.")
    interpreter.bindFunction("dump", Seq("expression"), expressionFunctions.dump, "Dumps the full object information for an expression.")
    interpreter.bindFunction("eval", Seq("expression"), expressionFunctions.eval, "Evalues an expression (same as examine).")
    interpreter.bindFunction("set", Seq("l", "r"), expressionFunctions.set, "Sets the left value to the expression on the right.")
    interpreter.bindFunction("locals", Nil, expressionFunctions.locals, "Prints all local variables in the current stack frame.")

    interpreter.bindFunction("bp", Seq("file", "line"), breakpointFunctions.createBreakpoint, "Creates a new breakpoint.")
    interpreter.bindFunction("bplist", Nil, breakpointFunctions.listBreakpoints, "Lists current breakpoints.")
    interpreter.bindFunction("bpclear", Seq("file", "line"), breakpointFunctions.clearBreakpoint, "Clears (deletes) the specified breakpoint or all breakpoints.")

    interpreter.bindFunction("mentry", Seq("class", "method"), methodFunctions.createEntry, "Creates a new method entry request.")
    interpreter.bindFunction("mentrylist", Nil, methodFunctions.listEntries, "Lists all existing method entry requests.")
    interpreter.bindFunction("mentryclear", Seq("class", "method"), methodFunctions.clearEntry, "Clears (deletes) any method entry on the class and method.")
    interpreter.bindFunction("mexit", Seq("class", "method"), methodFunctions.createExit, "Creates a new method exit request.")
    interpreter.bindFunction("mexitlist", Nil, methodFunctions.listExits, "Lists all existing method exit requests.")
    interpreter.bindFunction("mexitclear", Seq("class", "method"), methodFunctions.clearExit, "Clears (deletes) any method exit on the class and method.")

    interpreter.bindFunction("classes", Seq("filter"), classFunctions.classes, "Lists all classes by name, using an optional wildcard filter to list specific classes.")
    interpreter.bindFunction("methods", Seq("class"), classFunctions.methods, "Lists all methods for the specified class.")
    interpreter.bindFunction("fields", Seq("class"), classFunctions.fields, "Lists all fields for the specified class.")

    interpreter.bindFunction("threadgroups", Nil, threadGroupFunctions.threadsGroups, "Lists all thread groups.")
    interpreter.bindFunction("threadgroup", Seq("threadGroup"), threadGroupFunctions.threadGroup, "Sets the active thrad group with the specified name. No name clears the active thread group.")

    interpreter.bindFunction("list", Seq("size"), sourceFunctions.list, "Lists the source code for the file position located at the actively-suspended thread. Optional size indicates max lines to show on either side.")
    interpreter.bindFunction("sourcepath", Seq("sourcepath"), sourceFunctions.sourcepath, "Adds a new path to searched source paths or prints out current source paths.")
    interpreter.bindFunction("sourcepathclear", Nil, sourceFunctions.sourcepathClear, "Clears all current source paths.")
    interpreter.bindFunction("classpath", Nil, sourceFunctions.classpath, "Lists the classpath for each connected JVM.")

    interpreter.bindFunction("stepin", Nil, stepFunctions.stepIntoLine, "Steps into the current line where suspended to the frame below.")
    interpreter.bindFunction("stepout", Nil, stepFunctions.stepOutLine, "Steps out of the current line where suspended to the frame above.")
    interpreter.bindFunction("stepover", Nil, stepFunctions.stepOverLine, "Steps over the current line where suspended.")
    interpreter.bindFunction("stepinm", Nil, stepFunctions.stepIntoMin, "Steps into using min size where suspended to the frame below.")
    interpreter.bindFunction("stepoutm", Nil, stepFunctions.stepOutMin, "Steps out using min size where suspended to the frame above.")
    interpreter.bindFunction("stepoverm", Nil, stepFunctions.stepOverMin, "Steps over using min size where suspended.")

    interpreter.bindFunction("catch", Seq("filter"), exceptionFunctions.catchAll, "Detects the specified exception (or multiple using wildcards) or all exceptions if no filter provided.")
    interpreter.bindFunction("catchc", Seq("filter"), exceptionFunctions.catchCaught, "Detects the specified exception (or multiple using wildcards) or all exceptions if no filter provided. Only caught exceptions are detected.")
    interpreter.bindFunction("catchu", Seq("filter"), exceptionFunctions.catchUncaught, "Detects the specified exception (or multiple using wildcards) or all exceptions if no filter provided. Only uncaught exceptions are detected.")
    interpreter.bindFunction("catchlist", Nil, exceptionFunctions.listCatches, "Lists all listeners for exceptions.")
    interpreter.bindFunction("ignore", Seq("filter"), exceptionFunctions.ignoreAll, "Ignores (deletes) any listener for the specified exception or wildcard pattern or catchall if no filter specified. Only for caught and uncaught listeners.")
    interpreter.bindFunction("ignorec", Seq("filter"), exceptionFunctions.ignoreCaught, "Ignores (deletes) any listener for the specified exception or wildcard pattern or catchall if no filter specified. Only for caught-only listeners.")
    interpreter.bindFunction("ignoreu", Seq("filter"), exceptionFunctions.ignoreUncaught, "Ignores (deletes) any listener for the specified exception or wildcard pattern or catchall if no filter specified. Only for uncaught-only listeners.")

    interpreter.bindFunction("watch", Seq("class", "field"), watchpointFunctions.watchAll, "Watches access and modification for the specified class' field.")
    interpreter.bindFunction("watcha", Seq("class", "field"), watchpointFunctions.watchAccess, "Watches only access for the specified class' field.")
    interpreter.bindFunction("watchm", Seq("class", "field"), watchpointFunctions.watchModification, "Watches only modification for the specified class' field.")
    interpreter.bindFunction("watchlist", Nil, watchpointFunctions.listWatches, "Lists all listeners for access and modification of fields.")
    interpreter.bindFunction("unwatch", Seq("class", "field"), watchpointFunctions.unwatchAll, "Unwatches (deletes) access and modification watchpoints for the specified class' field.")
    interpreter.bindFunction("unwatcha", Seq("class", "field"), watchpointFunctions.unwatchAccess, "Unwatches (deletes) only access watchpoints for the specified class' field.")
    interpreter.bindFunction("unwatchm", Seq("class", "field"), watchpointFunctions.unwatchModification, "Unwatches (deletes) only modification watchpoints for the specified class' field.")

    // Set a dynamic prompt to use based on our state
    repl.activeTerminal.setPromptFunction(() => {
      val s = stateManager.state
      val debuggerInfo =
        if (s.activeDebugger.exists(_.isRunning)) "Running" else "Idle"

      val threadGroupInfo = s.activeThreadGroup.map(_.name).getOrElse("-")
      val threadInfo = s.activeThread.map(_.name).getOrElse("-")

      s"$debuggerInfo:$threadGroupInfo:$threadInfo> "
    })

    repl
  }
}
