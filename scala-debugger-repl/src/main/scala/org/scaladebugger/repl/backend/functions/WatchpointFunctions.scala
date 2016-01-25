package org.scaladebugger.repl.backend.functions

import com.sun.jdi.ThreadReference
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.lowlevel.wrappers.Implicits._
import org.scaladebugger.repl.backend.StateManager

import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a collection of functions for managing watchpoints.
 *
 * @param stateManager The manager whose state to share among functions
 */
class WatchpointFunctions(private val stateManager: StateManager) {

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
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    jvms.foreach(s => {
      println(s"<= ${s.uniqueId} =>")

      if (s.accessWatchpointRequests.nonEmpty) println("Access:")
      s.accessWatchpointRequests.zipWithIndex.foreach { case (awr, i) =>
        println(s"[${i+1}] ${awr.className}.${awr.fieldName}")
      }

      if (s.modificationWatchpointRequests.nonEmpty) println("Modification:")
      s.modificationWatchpointRequests.zipWithIndex.foreach { case (mwr, i) =>
        println(s"[${i+1}] ${mwr.className}.${mwr.fieldName}")
      }
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
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val fieldName = m.get("field").map(_.toString).getOrElse(
      throw new RuntimeException("Missing field argument!")
    )

    jvms.foreach(s => {
      if (includeAccess) s.onUnsafeAccessWatchpoint(
        className, fieldName, NoResume
      ).foreach(e => {
        val loc = e.location()
        val sn = loc.sourceName()
        val ln = loc.lineNumber()
        println(s"$className.$fieldName accessed ($sn:$ln)")
      })

      if (includeModification) s.onUnsafeModificationWatchpoint(
        className, fieldName, NoResume
      ).foreach(e => {
        val loc = e.location()
        val sn = loc.sourceName()
        val ln = loc.lineNumber()
        println(s"$className.$fieldName modified ($sn:$ln)")
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
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val fieldName = m.get("field").map(_.toString).getOrElse(
      throw new RuntimeException("Missing field argument!")
    )

    import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
    jvms.foreach(s => {
      if (removeAccess) s.onUnsafeAccessWatchpoint(
        className, fieldName
      ).close(data = CloseRemoveAll)

      if (removeModification) s.onUnsafeModificationWatchpoint(
        className, fieldName
      ).close(data = CloseRemoveAll)
    })
  }
}
