package org.scaladebugger.tool.backend.functions
import org.scaladebugger.api.profiles.traits.info.{FieldVariableInfoProfile, MethodInfoProfile, ReferenceTypeInfoProfile}
import org.scaladebugger.tool.backend.StateManager
import org.scaladebugger.tool.backend.utils.Regex

/**
 * Represents a collection of functions for managing methods.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class ClassFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
  /** Entrypoint for listing classes. */
  def classes(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    val filter = m.get("filter").map(_.toString).getOrElse("*")
    val fr = Regex.wildcardString(filter)

    val filterNot = m.get("filternot").map(_.toString).getOrElse("$^")
    val fnr = Regex.wildcardString(filterNot)

    @inline def classToString(c: ReferenceTypeInfoProfile): String =
      c.name + " " + c.genericSignature.map("(" + _ + ")").getOrElse("")

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")

      s.classes
        .filter(_.name.matches(fr))
        .filterNot(_.name.matches(fnr))
        .map(c => "-> " + classToString(c).trim)
        .foreach(writeLine)
    })
  }

  /** Entrypoint for listing methods for a class. */
  def methods(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val filter = m.get("filter").map(_.toString).getOrElse("*")
    val fr = Regex.wildcardString(filter)

    val filterNot = m.get("filternot").map(_.toString).getOrElse("$^")
    val fnr = Regex.wildcardString(filterNot)

    @inline def methodToString(m: MethodInfoProfile): String =
      m.name + "(" + m.parameterTypeInfo.map(_.name).mkString(",") + ")"

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")

      s.classOption(className)
        .map(_.allMethods)
        .getOrElse(Nil)
        .filter(_.name.matches(fr))
        .filterNot(_.name.matches(fnr))
        .map(m => "-> " + methodToString(m).trim)
        .foreach(writeLine)
    })
  }

  /** Entrypoint for listing fields for a class. */
  def fields(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) writeLine("No VM connected!")

    val className = m.get("class").map(_.toString).getOrElse(
      throw new RuntimeException("Missing class argument!")
    )

    val filter = m.get("filter").map(_.toString).getOrElse("*")
    val fr = Regex.wildcardString(filter)

    val filterNot = m.get("filternot").map(_.toString).getOrElse("$^")
    val fnr = Regex.wildcardString(filterNot)

    @inline def fieldToString(f: FieldVariableInfoProfile): String =
      f.name + ": " + f.typeName

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")

      s.classOption(className)
        .map(_.allFields)
        .getOrElse(Nil)
        .filter(_.name.matches(fr))
        .filterNot(_.name.matches(fnr))
        .map(f => "-> " + fieldToString(f).trim)
        .foreach(writeLine)
    })
  }
}
