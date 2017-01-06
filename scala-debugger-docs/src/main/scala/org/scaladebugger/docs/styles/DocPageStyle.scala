package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for the doc page.
 */
object DocPageStyle extends CascadingStyleSheet {
  /** To be placed in a <style> tag. */
  lazy val global: String =
    """
      |html, body {
      |  font-size: 1em;
      |  font-family: 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Geneva, Verdana, sans-serif;
      |  min-width: 880px;
      |  margin: 0;
      |  padding: 0;
      |}
    """.stripMargin

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

    overflowY := "auto",
    padding := "2em",

    a(
      color := "#232F3F",
      textDecoration := "none"
    ),

    img(
      maxWidth := "100%"
    )
  )
}
