package org.scaladebugger.repl.backend.functions
import acyclic.file

import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.lowlevel.requests.filters.ClassInclusionFilter
import org.scaladebugger.repl.backend.StateManager
import org.scaladebugger.repl.backend.utils.Regex

/**
 * Represents a collection of functions for managing threads.
 *
 * @param stateManager The manager whose state to share among functions
 */
class ExceptionFunctions(private val stateManager: StateManager) {
  /** Entrypoint for creating a request to detect caught/uncaught exceptions. */
  def catchAll(m: Map[String, Any]) = {
    handleCatchCommand(m, notifyCaught = true, notifyUncaught = true)
  }

  /** Entrypoint for creating a request to detect uncaught exceptions. */
  def catchUncaught(m: Map[String, Any]) = {
    handleCatchCommand(m, notifyCaught = false, notifyUncaught = true)
  }

  /** Entrypoint for creating a request to detect caught exceptions. */
  def catchCaught(m: Map[String, Any]) = {
    handleCatchCommand(m, notifyCaught = true, notifyUncaught = false)
  }

  /** Entrypoint for listing catch requests. */
  def listCatches(m: Map[String, Any]) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    jvms.foreach(s => {
      println(s"<= ${s.uniqueId} =>")
      s.exceptionRequests.foreach(eri => {
        val notifyText = newNotifyText(eri.notifyCaught, eri.notifyUncaught)
        val classInclusionFilter = eri.extraArguments.collect {
          case f: ClassInclusionFilter => f
        }.lastOption

        // Global catchall (no class filter)
        if (eri.isCatchall && classInclusionFilter.isEmpty) {
          println(s"Globally catch all $notifyText exceptions")

        // Wildcard catch
        } else if (eri.isCatchall && classInclusionFilter.nonEmpty) {
          val pattern = classInclusionFilter.get.classPattern
          println(s"Catch all $notifyText exceptions with pattern $pattern")

        // Specific class catch
        } else {
          val exceptionName = eri.className
          println(s"Catch all $notifyText for exception $exceptionName")
        }
      })
    })
  }

  /** Entrypoint for removing a request to detect caught/uncaught exceptions. */
  def ignoreAll(m: Map[String, Any]) = {
    handleIgnoreCommand(m, notifyCaught = true, notifyUncaught = true)
  }

  /** Entrypoint for removing a request to detect uncaught exceptions. */
  def ignoreUncaught(m: Map[String, Any]) = {
    handleIgnoreCommand(m, notifyCaught = false, notifyUncaught = true)
  }

  /** Entrypoint for removing a request to detect caught exceptions. */
  def ignoreCaught(m: Map[String, Any]) = {
    handleIgnoreCommand(m, notifyCaught = true, notifyUncaught = false)
  }

  /**
   * Removes an exception request based on input.
   *
   * @param m The map of input to the command
   * @param notifyCaught Used to match against an exception request
   * @param notifyUncaught Used to match against an exception request
   */
  private def handleIgnoreCommand(
    m: Map[String, Any],
    notifyCaught: Boolean,
    notifyUncaught: Boolean
  ) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    // NOTE: Wildcards handled directly by class inclusion filter
    val exceptionName = m.get("filter").map(_.toString)
    val extraArgs = exceptionName
      .filter(Regex.containsWildcards)
      .map(ClassInclusionFilter.apply)
      .toSeq

    import org.scaladebugger.api.profiles.Constants.CloseRemoveAll
    jvms.foreach(s => {
      if (exceptionName.isEmpty) {
        s.onUnsafeAllExceptions(
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          extraArgs: _*
        ).close(data = CloseRemoveAll)
      } else if (extraArgs.nonEmpty) {
        s.onUnsafeAllExceptions(
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          extraArgs: _*
        ).close(data = CloseRemoveAll)
      } else {
        s.onUnsafeException(
          exceptionName = exceptionName.get,
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          extraArgs: _*
        ).close(data = CloseRemoveAll)
      }
    })
  }

  /**
   * Creates an exception request and outputs information.
   *
   * @param m The map of input to the command
   * @param notifyCaught If true, request will listen for caught exceptions
   * @param notifyUncaught If true, request will listen for uncaught exceptions
   */
  private def handleCatchCommand(
    m: Map[String, Any],
    notifyCaught: Boolean,
    notifyUncaught: Boolean
  ) = {
    val jvms = stateManager.state.scalaVirtualMachines

    if (jvms.isEmpty) println("No VM connected!")

    // NOTE: Wildcards handled directly by class inclusion filter
    val exceptionName = m.get("filter").map(_.toString)
    val extraArgs = exceptionName
      .filter(Regex.containsWildcards)
      .map(ClassInclusionFilter.apply)
      .toSeq

    jvms.foreach(s => {
      val notifyText = newNotifyText(notifyCaught, notifyUncaught)

      val exceptionPipeline = if (exceptionName.isEmpty) {
        println(s"Watching for all $notifyText exceptions")
        s.onUnsafeAllExceptions(
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          NoResume +: extraArgs: _*
        )
      } else if (extraArgs.nonEmpty) {
        println(s"Watching for $notifyText exception pattern ${exceptionName.get}")
        s.onUnsafeAllExceptions(
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          NoResume +: extraArgs: _*
        )
      } else {
        println(s"Watching for $notifyText exception ${exceptionName.get}")
        s.onUnsafeException(
          exceptionName.get,
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          NoResume
        )
      }

      exceptionPipeline.foreach(e => {
        val cloc = Option(e.catchLocation())
        val loc = cloc.getOrElse(e.location())
        val fn = loc.sourceName()
        val ln = loc.lineNumber()

        val ex = e.exception()
        val exName = ex.referenceType().name()
        val ctext = if (cloc.nonEmpty) "Caught" else "Uncaught"

        println(s"$ctext $exName detected ($fn:$ln)")
      })
    })
  }

  private def newNotifyText(notifyCaught: Boolean, notifyUncaught: Boolean) = {
    if (notifyCaught && notifyUncaught) "caught and uncaught"
    else if (notifyCaught) "caught"
    else if (notifyUncaught) "uncaught"
    else "unknown"
  }
}
