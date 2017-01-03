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

  /** Represents the output directory of generated content. */
  val outputDir: ScallopOption[String] = opt[String](
    descr = "The output directory where content is generated and served",
    default = Some("out")
  )

  /** Represents the input directory of static and source content. */
  val inputDir: ScallopOption[String] = opt[String](
    descr = "The root input directory where source and static content is found",
    default = Some("docs")
  )

  /** Represents the directory of source content. */
  val srcDir: ScallopOption[String] = opt[String](
    descr = "The source directory (relative to input directory) where source content is found",
    default = Some("src")
  )

  /** Represents the directory of static content. */
  val staticDir: ScallopOption[String] = opt[String](
    descr = "The static directory (relative to input directory) where static content is found",
    default = Some("static")
  )

  /** Represents whether or not to serve the docs using a local server. */
  val serve: ScallopOption[Boolean] = opt[Boolean](
    descr = "If true, serves the generated docs",
    default = Some(false)
  )

  /**
   * Represents files that serve as defaults when accessing a directory.
   *
   * E.g. '/my/path/' becomes '/my/path/index.html'
   */
  val indexFiles: ScallopOption[List[String]] = opt[List[String]](
    descr = "Files that serve as defaults when accessing a directory",
    default = Some(List("index.html", "index.htm"))
  )

  // Display our default values in our help menu
  appendDefaultToDescription = true

  // Process arguments
  verify()
}

