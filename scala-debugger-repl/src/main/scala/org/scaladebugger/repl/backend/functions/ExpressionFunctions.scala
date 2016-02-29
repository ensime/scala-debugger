package org.scaladebugger.repl.backend.functions
import acyclic.file

import org.scaladebugger.api.lowlevel.wrappers.Implicits._
import org.scaladebugger.api.lowlevel.wrappers.ValueWrapper
import org.scaladebugger.repl.backend.StateManager

import scala.collection.JavaConverters._

/**
 * Represents a collection of functions for examining the remote JVM.
 *
 * @param stateManager The manager whose state to share among functions
 */
class ExpressionFunctions(private val stateManager: StateManager) {

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
import acyclic.file
<class pattern>: a class name with a leading or trailing wildcard ('*')
<thread id>: thread number as reported in the 'threads' command
<expr>: a Java(TM) Programming Language expression.
Most common syntax is supported.

Startup commands can be placed in either "jdb.ini" or ".jdbrc"
in user.home or user.dir
   */
  /*
  print <expr>              -- print value of expression
  dump <expr>               -- print all object information
    eval <expr>               -- evaluate expression (same as print)
      set <lvalue> = <expr>     -- assign new value to field/variable/array element
        locals                    -- print all local variables in current stack frame
*/

  /** Entrypoint for printing the value of an expression. */
  def examine(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) println("No VM connected!")

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) println("No active thread!")

    val expression = m.get("expression").map(_.toString).getOrElse(
      throw new RuntimeException("Missing expression argument!")
    )

    thread.foreach(t => {
      t.suspend()

      // TODO: Support expressions other than variable names
      val variableName = expression

      import org.scaladebugger.api.lowlevel.wrappers.Implicits._
      val objMap = t.frame(0).localVisibleVariableMap()
      val value = objMap.find(_._1.name() == variableName).map(_._2.value())

      value.foreach(println)

      t.resume()
    })
  }

  /** Entrypoint for printing all object information. */
  def dump(m: Map[String, Any]) = {
    println("Not implemented!")
  }

  /** Entrypoint to evaluate an expression (same as print). */
  def eval(m: Map[String, Any]) = {
    println("Not implemented!")
  }

  /** Entrypoint for assigning a new value to a field/variable/array element. */
  def set(m: Map[String, Any]) = {
    println("Not implemented!")
  }

  /** Entrypoint for printing all local variables in the current stack frame. */
  def locals(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) println("No VM connected!")

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) println("No thread selected!")

    thread.foreach(t => {
      if (!t.isSuspended) println("Active thread is not suspended!")
      else {
        import org.scaladebugger.api.lowlevel.wrappers.Implicits._
        val fields = t.frame(0).thisVisibleFieldMap()
        val obj = t.frame(0).localVisibleVariableMap()

        println("FIELDS:")
        fields.map(t => (t._1.name(), new ValueWrapper(t._2).toString(5)))
          .foreach(println)

        println("LOCALS:")
        obj.map(t => (t._1.name(), t._2.value())).foreach(println)
      }
    })
  }
}
