package org.scaladebugger.tool

import java.io.File

import org.rogach.scallop.ScallopConf
import org.scaladebugger.api.debuggers.Debugger
import org.scaladebugger.api.profiles.java.JavaDebugProfile
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile

/**
 * Represents the CLI configuration for the Scala debugger tool.
 *
 * @param arguments The list of arguments fed into the CLI (same
 *                  arguments that are fed into the main method)
 */
class Config(arguments: Seq[String]) extends ScallopConf(arguments) {
  private val candidateProfiles = Seq(
    JavaDebugProfile.Name,
    Scala210DebugProfile.Name
  )
  private val historyFilePath =
    System.getProperty("user.home", ".") + File.separator + ".sdb_history"

  /** Represents the profile name that should be used by default. */
  val defaultProfile = opt[String](
    descr = Seq(
      "Represents the debugger profile to use by default",
      "Select from " + candidateProfiles.mkString(",")
    ).mkString("; "),
    validate = candidateProfiles.contains(_: String),
    default = Some(Scala210DebugProfile.Name)
  )

  /** Represents the history file location. */
  val historyFile = opt[String](
    descr = "Represents the location of the file to store history",
    default = Some(historyFilePath)
  )

  /** Represents the maximum number of lines to keep in history. */
  val historyMaxLines = opt[Int](
    descr = Seq(
      "Represents the maximum number of lines to keep in history",
      "with -1 being unlimited and 0 being none"
    ).mkString(" "),
    default = Some(1000)
  )

  /** Represents whether or not to print undefined values in the terminal. */
  val printUndefined = opt[Boolean](
    descr = Seq(
      "If true, prints \"undefined\" for any undefined variable in terminal",
      "; otherwise, nothing is printed"
    ).mkString(" "),
    default = Some(false)
  )

  /** Represents the setting to force usage of the fallback terminal. */
  val forceUseFallback = opt[Boolean](
    descr = "If true, forces the use of the fallback terminal",
    default = Some(false)
  )

  // Display our default values in our help menu
  appendDefaultToDescription = true

  // Process arguments
  verify()
}
