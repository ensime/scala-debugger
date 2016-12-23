package org.scaladebugger.docs

import org.scaladebugger.docs.layouts.FrontPage
import org.scaladebugger.docs.styles.FrontPageStyle

/**
 * Main entrypoint for generating and serving docs.
 */
object Main {
  def main(args: Array[String]): Unit = {
    val config = new Config(args)

    // Generate before other actions if indicated
    if (config.generate()) {
      // TODO: Create steps to load all content
      //       and generate pages accordingly
    }

    // Load latest version of generated docs
    // TODO: Load pages from output directory of some kind
    lazy val pages = Map(
      "/" -> FrontPage().toString
    )

    if (config.serve()) {
      import unfiltered.request.{Path => UFPath, _}
      import unfiltered.response._
      val hostedContent = unfiltered.filter.Planify {
        case GET(UFPath(path)) => pages.get(path) match {
          case Some(page) => ResponseString(page)
          case None => ResponseString(s"Unknown page: $path")
        }
        case _ =>
          ResponseString("Unknown request")
      }
      unfiltered.jetty.Server.http(8080).plan(hostedContent).run()
    } else if (config.publish()) {
      // TODO: Implement publish using Scala process to run git
    } else {
      config.printHelp()
    }
  }
}
