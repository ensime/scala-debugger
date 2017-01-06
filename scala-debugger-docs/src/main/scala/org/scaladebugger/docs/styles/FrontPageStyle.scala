package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for the front page.
 */
object FrontPageStyle extends CascadingStyleSheet {
  /** To be placed in a <style> tag. */
  lazy val global: String =
    """
      |* {
      |  margin: 0;
      |  padding: 0;
      |}
      |
      |html, body {
      |  font-size: 1em;
      |  font-family: 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Geneva, Verdana, sans-serif;
      |  min-width: 880px;
      |}
    """.stripMargin
}
