package org.scaladebugger.tool.backend.functions
import acyclic.file
import org.scaladebugger.api.lowlevel.events.filters.WildcardPatternFilter
import org.scaladebugger.api.lowlevel.events.misc.NoResume
import org.scaladebugger.api.lowlevel.exceptions.ExceptionRequestInfo
import org.scaladebugger.api.profiles.traits.info.LocationInfoProfile
import org.scaladebugger.tool.backend.StateManager
import org.scaladebugger.tool.backend.utils.Regex

/**
 * Represents a collection of functions for managing threads.
 *
 * @param stateManager The manager whose state to share among functions
 * @param writeLine Used to write output to the terminal
 */
class ExceptionFunctions(
  private val stateManager: StateManager,
  private val writeLine: String => Unit
) {
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
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      val dsvm = stateManager.state.dummyScalaVirtualMachine
      jvms = Seq(dsvm)
    }

    def pstr(p: Boolean): String = if (p) "Pending" else "Active"

    jvms.foreach(s => {
      writeLine(s"<= JVM ${s.uniqueId} =>")
      s.exceptionRequests.foreach(eri => {
        val notifyText = newNotifyText(eri.notifyCaught, eri.notifyUncaught)
        val wildcardPatternFilter = eri.extraArguments.collect {
          case f: WildcardPatternFilter => f
        }.lastOption

        val p = s"(${pstr(eri.isPending)})"

        // Global catchall (no class filter)
        if (eri.isCatchall && wildcardPatternFilter.isEmpty) {
          writeLine(s"Globally catch all $notifyText exceptions $p")

        // Wildcard catch
        } else if (eri.isCatchall && wildcardPatternFilter.nonEmpty) {
          val pattern = wildcardPatternFilter.get.pattern
          writeLine(s"Catch all $notifyText exceptions with pattern $pattern $p")

        // Specific class catch
        } else {
          val exceptionName = eri.className
          writeLine(s"Catch all $notifyText for exception $exceptionName $p")
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
   * @param notifyCaught If true, will ignore requests with notify caught
   *                     enabled
   * @param notifyUncaught If true, will ignore requests with notify uncaught
   *                       enabled
   */
  private def handleIgnoreCommand(
    m: Map[String, Any],
    notifyCaught: Boolean,
    notifyUncaught: Boolean
  ) = {
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      val dsvm = stateManager.state.dummyScalaVirtualMachine
      jvms = Seq(dsvm)
    }

    // Name is either exact, wildcard, or all exceptions
    val exceptionName = m.get("filter").map(_.toString)
      .getOrElse("*")

    // Matches name based on exact match or wildcard expression
    val nameMatch =
      if (Regex.containsWildcards(exceptionName))
        (name: String) => name.matches(Regex.wildcardString(exceptionName))
      else
        (name: String) => name == exceptionName

    jvms.foreach(s => {
      // Find and remove each match
      s.exceptionRequests.filter(e => {
        val nc = notifyCaught == e.notifyCaught
        val nu = notifyUncaught == e.notifyUncaught
        nameMatch(e.className) && (nc || notifyCaught) && (nu || notifyUncaught)
      }).foreach(e => s.removeExceptionRequestWithArgs(
        exceptionName = e.className,
        notifyCaught = e.notifyCaught,
        notifyUncaught = e.notifyUncaught,
        e.extraArguments: _*
      ))
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
    @volatile var jvms = stateManager.state.scalaVirtualMachines
    if (jvms.isEmpty) {
      val dsvm = stateManager.state.dummyScalaVirtualMachine
      jvms = Seq(dsvm)
    }

    // NOTE: Wildcards handled directly by class inclusion filter
    val exceptionName = m.get("filter").map(_.toString)
    val extraArgs = exceptionName
      .filter(Regex.containsWildcards)
      .map(WildcardPatternFilter.apply)
      .toSeq

    jvms.foreach(s => {
      val notifyText = newNotifyText(notifyCaught, notifyUncaught)

      val exceptionPipeline = if (exceptionName.isEmpty) {
        writeLine(s"Watching for all $notifyText exceptions")
        s.getOrCreateAllExceptionsRequest(
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          NoResume +: extraArgs: _*
        )
      } else if (extraArgs.nonEmpty) {
        writeLine(s"Watching for $notifyText exception pattern ${exceptionName.get}")
        s.getOrCreateAllExceptionsRequest(
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          NoResume +: extraArgs: _*
        )
      } else {
        writeLine(s"Watching for $notifyText exception ${exceptionName.get}")
        s.getOrCreateExceptionRequest(
          exceptionName.get,
          notifyCaught = notifyCaught,
          notifyUncaught = notifyUncaught,
          NoResume
        )
      }

      exceptionPipeline.foreach(e => {
        val cloc = e.catchLocation
        val loc = cloc.getOrElse(e.location)
        val fn = loc.sourceName
        val ln = loc.lineNumber

        val ex = e.exception
        val exName = ex.referenceType.name
        val ctext = if (cloc.nonEmpty) "Caught" else "Uncaught"

        writeLine(s"$ctext $exName detected ($fn:$ln)")
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
