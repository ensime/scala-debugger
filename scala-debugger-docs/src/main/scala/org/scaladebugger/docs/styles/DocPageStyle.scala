package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for the doc page.
 */
object DocPageStyle extends CascadingStyleSheet {
  lazy val bodyCls: Cls = cls(
    display := "flex",
    flexDirection := "column",
    alignItems := "stretch",
    height := "100vh",

    header(flex := "0 0 auto"),
    footer(flex := "0 0 auto")
  )

  lazy val mainContent: Cls = cls(
    display := "inline-block",

    background := "#EAEAEC",
    color := "#696969",

    position := "relative",
    overflowY := "auto"
  )
}
