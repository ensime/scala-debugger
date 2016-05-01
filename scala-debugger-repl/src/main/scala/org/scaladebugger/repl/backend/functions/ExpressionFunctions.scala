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
