package org.scaladebugger.repl
import acyclic.file

import java.io.File

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.language.interpreters.DebuggerInterpreter
import org.scaladebugger.repl.backend.{StateManager, ArgumentParser}
import org.scaladebugger.repl.backend.functions._
import org.scaladebugger.repl.frontend.Repl

object Main {
  def main(args: Array[String]): Unit = {
    val results = ArgumentParser.parse(args)

    // Attempt to load the JDI into our system
    if (!JDITools.tryLoadJdi()) return

    if (results.help) {
      Console.out.println("No help available.")
    } else {
      val interpreter = new DebuggerInterpreter

      val stateManager = new StateManager
      val debuggerFunctions = new DebuggerFunctions(stateManager)
      val threadFunctions = new ThreadFunctions(stateManager)
      val expressionFunctions = new ExpressionFunctions(stateManager)
      val breakpointFunctions = new BreakpointFunctions(stateManager)
      val methodFunctions = new MethodFunctions(stateManager)
      val classFunctions = new ClassFunctions(stateManager)
      val threadGroupFunctions = new ThreadGroupFunctions(stateManager)
      val sourceFunctions = new SourceFunctions(stateManager)
      val stepFunctions = new StepFunctions(stateManager)
      val exceptionFunctions = new ExceptionFunctions(stateManager)
      val watchpointFunctions = new WatchpointFunctions(stateManager)

      interpreter.bindFunction("attach", Seq("port", "hostname", "timeout"), debuggerFunctions.attach, "Attaches to an already-running JVM process.")
      interpreter.bindFunction("launch", Seq("class", "suspend"), debuggerFunctions.launch, "Launches a new JVM process and attaches to it.")
      interpreter.bindFunction("listen", Seq("port", "hostname"), debuggerFunctions.listen, "Listens for incoming JVM connections.")
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
      interpreter.bindFunction("mexitlist", Nil, methodFunctions.listEntries, "Lists all existing method exit requests.")
      interpreter.bindFunction("mexitclear", Seq("class", "method"), methodFunctions.clearExit, "Clears (deletes) any method exit on the class and method.")

      interpreter.bindFunction("classes", Seq("filter"), classFunctions.classes, "Lists all classes by name, using an optional wildcard filter to list specific classes.")
      interpreter.bindFunction("methods", Seq("class"), classFunctions.methods, "Lists all methods for the specified class.")
      interpreter.bindFunction("fields", Seq("class"), classFunctions.fields, "Lists all fields for the specified class.")

      interpreter.bindFunction("threadgroups", Nil, threadGroupFunctions.threadsGroups, "Lists all thread groups.")
      interpreter.bindFunction("threadgroup", Seq("threadGroup"), threadGroupFunctions.threadGroup, "Sets the active thrad group with the specified name. No name clears the active thread group.")

      interpreter.bindFunction("list", Nil, sourceFunctions.list, "Lists the source code for the file position located at the actively-suspended thread.")
      interpreter.bindFunction("sourcepath", Seq("sourcepath"), sourceFunctions.sourcepath, "Adds a new path to searched source paths or prints out current source paths.")
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

      stateManager.addSourcePath(new File("/home/senkwich/projects/scala-debugger/scala-debugger-test/src/main/scala").toURI)
      val repl = new Repl(
        interpreter,
        stateManager,
        forceUseFallback = results.forceUseFallback
      )

      // Set a dynamic prompt to use based on our state
      repl.activeTerminal.setPromptFunction(() => {
        val s = stateManager.state
        val debuggerInfo =
          if (s.activeDebugger.exists(_.isRunning)) "Running" else "Idle"

        val threadGroupInfo = s.activeThreadGroup.map(_.name).getOrElse("-")
        val threadInfo = s.activeThread.map(_.name).getOrElse("-")

        s"$debuggerInfo:$threadGroupInfo:$threadInfo> "
      })
      repl.start() // Blocking call, only completing upon exiting REPL

      // Clear state, which will shutdown active debugger and allow proper exit
      stateManager.clear()
    }
  }
}
