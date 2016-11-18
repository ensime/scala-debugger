package org.scaladebugger.tool.backend.functions
import acyclic.file
import org.scaladebugger.api.lowlevel.wrappers.Implicits._
import org.scaladebugger.api.lowlevel.wrappers.ValueWrapper
import org.scaladebugger.api.profiles.traits.info.{ThreadInfoProfile, VariableInfoProfile}
import org.scaladebugger.tool.backend.StateManager

import scala.collection.JavaConverters._

/**
 * Represents a collection of functions for examining the remote JVM.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class ExpressionFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for printing the value of an expression. */
  def examine(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) writeLine("No VM connected!")

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No active thread!")

    val expression = m.get("expression").map(_.toString).getOrElse(
      throw new RuntimeException("Missing expression argument!")
    )

    thread.foreach(t => {
      // TODO: Bundle suspend and resume into single function
      //       that executes a block and resumes regardless
      t.suspend()

      val variable = lookupVariable(t, expression)

      // Generate "pretty string" of variable
      // and then produce a list of its fields if it is an object
      variable.map(_.toPrettyString).foreach(writeLine)
      variable.map(_.toValueInfo)
        .filter(_.isObject)
        .filterNot(_.isString)
        .map(_.toObjectInfo)
        .map(_.fields)
        .map(_.map("-> " + _.toPrettyString))
        .foreach(_.foreach(writeLine))

      t.resume()
    })
  }

  /** Entrypoint for printing all object information. */
  def dump(m: Map[String, Any]) = {
    writeLine("Not implemented!")
  }

  /** Entrypoint to evaluate an expression (same as print). */
  def eval(m: Map[String, Any]) = {
    writeLine("Not implemented!")
  }

  /** Entrypoint for assigning a new value to a field/variable/array element. */
  def set(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) throw new RuntimeException("No VM connected!")

    val thread = stateManager.state.activeThread
    if (thread.isEmpty) throw new RuntimeException("No active thread!")
    if (!thread.get.status.isSuspended)
      throw new RuntimeException("Active thread not suspended!")

    // Represents the expression in the form of obj.field
    val l = m.get("l").map(_.toString).getOrElse(
      throw new RuntimeException("Missing l argument!")
    )

    // Represents the value
    val r = m.get("r").map(_.toString).getOrElse(
      throw new RuntimeException("Missing r argument!")
    )

    thread.foreach(t => {
      // TODO: Bundle suspend and resume into single function
      //       that executes a block and resumes regardless
      t.suspend()

      val variable = lookupVariable(t, l)
      variable.foreach(v => {
        // TODO: Support assigning object or value that is
        //       already remote
        val i = v.typeInfo.castLocal(r) match {
          case st: String => v.createRemotely(st)
          case av         => v.createRemotely(av.asInstanceOf[AnyVal])
        }

        v.trySetValueFromInfo(i).failed.map(_.toString).foreach(writeLine)
      })

      t.resume()
    })
  }

  /** Entrypoint for printing all local variables in the current stack frame. */
  def locals(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) writeLine("No VM connected!")

    // TODO: Support "thread" as argument
    val thread = stateManager.state.activeThread
    if (thread.isEmpty) writeLine("No thread selected!")

    thread.foreach(t => {
      if (!t.status.isSuspended) writeLine("Active thread is not suspended!")
      else {
        val frame = t.topFrame

        writeLine("[FIELDS]")
        frame.fieldVariables.map("-> " + _.toPrettyString).foreach(writeLine)

        writeLine("[LOCALS]")
        frame.localVariables.map("-> " + _.toPrettyString).foreach(writeLine)
      }
    })
  }

  private def lookupVariable(
    thread: ThreadInfoProfile,
    expression: String
  ): Option[VariableInfoProfile] = {
    // Support myObject.myObjField.myField notation
    val variableChain = expression.split('.').map(_.trim)
    if (variableChain.isEmpty)
      throw new RuntimeException("Invalid expression provided!")

    val firstVariable = variableChain.headOption
      .flatMap(thread.findVariableByName)

    variableChain.tail.foldLeft(firstVariable) { case (v, name) =>
      v.map(_.toValueInfo)
        .filter(_.isObject)
        .map(_.toObjectInfo)
        .flatMap(_.fieldOption(name))
    }
  }
}
