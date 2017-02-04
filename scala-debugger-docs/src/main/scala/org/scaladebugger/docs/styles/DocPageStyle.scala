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
      |  margin: 0;
      |  padding: 0;
      |}
      |
      |html, body {
      |  font-size: 1em;
      |  font-family: 'Lucida Grande', 'Lucida Sans Unicode', 'Lucida Sans', Geneva, Verdana, sans-serif;
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

  lazy val viewArea: Cls = cls(
    maxWidth := "75em"
  )

  lazy val mainContent: Cls = cls(
    display := "inline-block",

    background := "#EAEAEC",
    color := "#3B3E43",

    overflowY := "auto",
    padding := "2em",

    a(
      color := "#232F3F",
      textDecoration := "none",
      fontWeight := "bold"
    ),

    img(
      // Used to center image
      display := "block",
      marginLeft := "auto",
      marginRight := "auto",

      maxWidth := "100%"
    ),

    Selector(".hljs")(
      padding := "1em",
      borderRadius := "8px",
      background := "#3B3E43"
    ),

    pre(
      code(
        Selector(":not(.hljs)")(
          display := "inline-block",
          background := "#ECF0F1",
          color := "#3B3E43",
          border := "1px solid #979797",
          borderRadius := "8px",
          overflow := "auto",
          padding := "1em",
          fontWeight := "inherit"
        )
      )
    ),

    code(
      Selector(":not(.hljs)")(
        color := "#9B0000",
        fontWeight := "bold"
      )
    ),

    table(
      background := "#FBFBFB",
      emptyCells := "show",
      width := "100%",
      height := "auto",
      border := "1px solid #CBCBCB",
      borderRadius := "8px",
      //borderCollapse := "collapse",
      borderSpacing := "0",
      marginTop := "1em",
      marginBottom := "1em",

      thead(
        background := "#D8D8D8",
        color := "#3B3E43",
        textAlign := "left",
        verticalAlign := "bottom"
      ),

      th(
        padding := "0.5em 1.0em",
        borderLeft := "1px solid #CBCBCB",
        borderWidth := "0px 0px 0px 1px",
        fontSize := "inherit",
        margin := "0em",
        overflow := "visible",

        &.firstChild(
          borderLeftWidth := "0em"
        )
      ),

      td(
        padding := "0.5em 1.0em",
        borderLeft := "1px solid #CBCBCB",
        borderWidth := "0px 0px 0px 1px",
        fontSize := "inherit",
        margin := "0em",
        overflow := "visible",

        &.firstChild(
          borderLeftWidth := "0em"
        )
      )
    )
  )
}
