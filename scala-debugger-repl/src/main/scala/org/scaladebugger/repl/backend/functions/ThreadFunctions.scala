package org.scaladebugger.repl.backend.functions

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.wrappers.Implicits._
import org.scaladebugger.repl.backend.StateManager

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a collection of functions for managing threads.
 *
 * @param stateManager The manager whose state to share among functions
 */
class ThreadFunctions(private val stateManager: StateManager) {

  /*
** command list **
connectors                -- list available connectors and transports in this VM

run [class [args]]        -- start execution of application's main class

threads [threadgroup]     -- list threads
thread <thread id>        -- set default thread
suspend [thread id(s)]    -- suspend threads (default: all)
resume [thread id(s)]     -- resume threads (default: all)
where [<thread id> | all] -- dump a thread's stack
wherei [<thread id> | all]-- dump a thread's stack, with pc info
up [n frames]             -- move up a thread's stack
down [n frames]           -- move down a thread's stack
kill <thread id> <expr>   -- kill a thread with the given exception object
interrupt <thread id>     -- interrupt a thread

print <expr>              -- print value of expression
dump <expr>               -- print all object information
eval <expr>               -- evaluate expression (same as print)
set <lvalue> = <expr>     -- assign new value to field/variable/array element
locals                    -- print all local variables in current stack frame

classes                   -- list currently known classes
class <class id>          -- show details of named class
methods <class id>        -- list a class's methods
fields <class id>         -- list a class's fields

threadgroups              -- list threadgroups
threadgroup <name>        -- set current threadgroup

stop in <class id>.<method>[(argument_type,...)]
                          -- set a breakpoint in a method
stop at <class id>:<line> -- set a breakpoint at a line
clear <class id>.<method>[(argument_type,...)]
                          -- clear a breakpoint in a method
clear <class id>:<line>   -- clear a breakpoint at a line
clear                     -- list breakpoints
catch [uncaught|caught|all] <class id>|<class pattern>
                          -- break when specified exception occurs
ignore [uncaught|caught|all] <class id>|<class pattern>
                          -- cancel 'catch' for the specified exception
watch [access|all] <class id>.<field name>
                          -- watch access/modifications to a field
unwatch [access|all] <class id>.<field name>
                          -- discontinue watching access/modifications to a field
trace [go] methods [thread]
                          -- trace method entries and exits.
                          -- All threads are suspended unless 'go' is specified
trace [go] method exit | exits [thread]
                          -- trace the current method's exit, or all methods' exits
                          -- All threads are suspended unless 'go' is specified
untrace [methods]         -- stop tracing method entrys and/or exits
step                      -- execute current line
step up                   -- execute until the current method returns to its caller
stepi                     -- execute current instruction
next                      -- step one line (step OVER calls)
cont                      -- continue execution from breakpoint

list [line number|method] -- print source code
use (or sourcepath) [source file path]
                          -- display or change the source path
exclude [<class pattern>, ... | "none"]
                          -- do not report step or method events for specified classes
classpath                 -- print classpath info from target VM

monitor <command>         -- execute command each time the program stops
monitor                   -- list monitors
unmonitor <monitor#>      -- delete a monitor
read <filename>           -- read and execute a command file

lock <expr>               -- print lock info for an object
threadlocks [thread id]   -- print lock info for a thread

pop                       -- pop the stack through and including the current frame
reenter                   -- same as pop, but current frame is reentered
redefine <class id> <class file name>
                          -- redefine the code for a class

disablegc <expr>          -- prevent garbage collection of an object
enablegc <expr>           -- permit garbage collection of an object

!!                        -- repeat last command
<n> <command>             -- repeat command n times
# <command>               -- discard (no-op)
help (or ?)               -- list commands
version                   -- print version information
exit (or quit)            -- exit debugger

<class id>: a full class name with package qualifiers
<class pattern>: a class name with a leading or trailing wildcard ('*')
<thread id>: thread number as reported in the 'threads' command
<expr>: a Java(TM) Programming Language expression.
Most common syntax is supported.

Startup commands can be placed in either "jdb.ini" or ".jdbrc"
in user.home or user.dir
   */

  /** Entrypoint for listing thread information for connected JVMs. */
  def threads(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    // Optional thread group (argument has priority, followed by active)
    val threadGroup = m.get("threadGroup").map(_.toString)
      .orElse(stateManager.state.activeThreadGroup.map(_.name()))

    jvms.foreach(s => {
      val threadGroups = s.underlyingVirtualMachine
        .allThreads().asScala.map(_.threadGroup()).distinct
        .filter(tg => threadGroup.isEmpty || tg.name() == threadGroup.get)

      println(s"<= JVM ${s.uniqueId} =>")
      threadGroups.foreach(tg => {
        println("Group " + tg.name() + ":")
        tg.threads().asScala.foreach(t => {
          val rName = t.referenceType().name()
          val id = "0x" + t.uniqueID().toHexString
          val name = t.name()
          val status = t.statusString
          val suspended = if (t.isSuspended) "suspended" else ""
          val atBreakpoint = if (t.isAtBreakpoint) "at breakpoint" else ""
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
        val thread = jvms.view.flatMap(_.underlyingVirtualMachine.allThreads().asScala).collectFirst {
          case t if t.name() == name => t
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
      val threads = jvms
        .flatMap(_.underlyingVirtualMachine.allThreads().asScala)
        .filter(t => threadNames.contains(t.name()))

      threads.foreach(_.suspend())

    // Suspend entire JVM
    } else {
      jvms.foreach(_.underlyingVirtualMachine.suspend())
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
      val threads = jvms
        .flatMap(_.underlyingVirtualMachine.allThreads().asScala)
        .filter(t => threadNames.contains(t.name()))

      threads.foreach(_.resume())

    // Resume entire JVM
    } else {
      jvms.foreach(_.underlyingVirtualMachine.resume())
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
      val threads = jvms
        .flatMap(_.underlyingVirtualMachine.allThreads().asScala)
        .filter(t => threadNames.contains(t.name()))

      threads.foreach(printFrameStack)

    // Dump stack for current thread
    } else {
      stateManager.state.activeThread match {
        case Some(t) => printFrameStack(t)
        case None => println("No active thread!")
      }
    }
  }

  private def printFrameStack(threadReference: ThreadReference) = {
    threadReference.suspend()

    Try(threadReference.frames())
      .map(_.asScala)
      .map(_.map(_.location()).zipWithIndex)
      .map(_.map { case (l, i) =>
        s"[${i+1}] " +
        s"${l.declaringType().name()}.${l.method().name()} " +
        s"(${l.sourceName()}:${l.lineNumber()})"
      })
      .foreach(frames => println(frames.mkString("\n")))

    threadReference.resume()
  }
}
