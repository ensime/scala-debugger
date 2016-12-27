package org.scaladebugger.docs

import org.rogach.scallop.{ScallopConf, ScallopOption}

/**
 * Represents the CLI configuration for the Scala debugger tool.
 *
 * @param arguments The list of arguments fed into the CLI (same
 *                  arguments that are fed into the main method)
 */
class Config(arguments: Seq[String]) extends ScallopConf(arguments) {
  /** Represents whether or not to publish the built docs. */
  val publish: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, publishes the generated docs",
    default = Some(false)
  )

  /** Represents whether or not to generate the docs. */
  val generate: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, regenerates the docs",
    default = Some(false)
  )

  /** Represents whether or not to serve the docs using a local server. */
  val serve: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, serves the generated docs",
    default = Some(false)
  )

  // Display our default values in our help menu
  appendDefaultToDescription = true

  // Process arguments
  verify()
}

