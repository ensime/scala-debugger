package org.scaladebugger.tool

import org.rogach.scallop.ScallopConf
import org.scaladebugger.api.debuggers.Debugger
import org.scaladebugger.api.profiles.pure.PureDebugProfile
import org.scaladebugger.api.profiles.scala210.Scala210DebugProfile

/**
 * Represents the CLI configuration for the Scala debugger tool.
 *
 * @param arguments The list of arguments fed into the CLI (same
 *                  arguments that are fed into the main method)
 */
class Config(arguments: Seq[String]) extends ScallopConf(arguments) {
  private val candidateProfiles = Seq(
    PureDebugProfile.Name,
    Scala210DebugProfile.Name
  )

  /** Represents the profile name that should be used by default. */
  val defaultProfile = opt[String](
    descr = Seq(
      "Represents the debugger profile to use by default",
      "Select from " + candidateProfiles.mkString(",")
    ).mkString("; "),
    validate = candidateProfiles.contains(_: String),
    default = Some(Debugger.DefaultProfileName)
  )

  /** Represents the setting to force usage of the fallback terminal. */
  val forceUseFallback = opt[Boolean](
    descr = "If true, forces the use of the fallback terminal",
    default = Some(false)
  )

  verify()
}
