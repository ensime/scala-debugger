package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for all pages.
 */
object PageStyle extends CascadingStyleSheet {
  import scalatags.Text.styles2.{content => afterContent}

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

  //
  // HERO CSS
  //

  lazy val heroTitle: Cls = cls(
    display := "flex",
    alignItems := "center",
    justifyContent := "space-around",
    fontSize := "5em",

    img(
      padding := "0.5em"
    )
  )

  lazy val heroSubtitle: Cls = cls(
    fontFamily := "Baskerville, 'Baskerville Old Face', 'Hoefler Text', Garamond, 'Times New Roman', serif",
    fontSize := "2em",
    fontStyle := "italic",
    margin := "0em 0em 1.5em 0em",
    whiteSpace := "nowrap"
  )

  //
  // SECTION CSS
  //

  lazy val sectionDark: Cls = cls(
    background := "#3B3E43",
    color := "#EBF0F1",
    a(color := "#EBF0F1")
  )

  lazy val sectionLight: Cls = cls(
    background := "#EAEAEC",
    color := "#3B3E43",
    a(color := "#3B3E43")
  )

  lazy val section: Cls = cls(
    width := "100%",
    minHeight := "33vh"
  )

  lazy val sectionContent: Cls = cls(
    display := "flex",
    flexDirection := "column",
    alignItems := "center",
    padding := "3em 2em",
    //height := "calc(100% - 6em)",

    h1(
      fontSize := "5em",
      margin := "0em"
    )
  )

  //
  // FOOTER CSS
  //

  lazy val footerCls: Cls = cls(
    width := "100%",
    height := "auto"
  )

  lazy val footerContent: Cls = cls(
    display := "flex",
    flexDirection := "row",
    flexWrap := "nowrap",
    justifyContent := "flex-end",
    alignItems := "center",

    padding := "1em 1em",
    fontSize := "0.7em"
  )

  //
  // MISC CSS
  //

  lazy val buttonCls: Cls = cls(
    background := "#232F3F",
    color := "#ECF0F1",
    padding := "1em 1.5em",
    borderRadius := "8px",
    overflow := "hidden",
    textOverflow := "ellipsis",
    whiteSpace := "nowrap",

    a(
      color := "#ECF0F1",
      textDecoration := "none"
    )
  )

  lazy val videoCls: Cls = cls(
    width := "100%"
  )

  lazy val fitContainer: Cls = cls(
    width := "100%",
    height := "100%"
  )

  //
  // LINED CONTENT CSS
  //

  lazy val linedContent: Cls = cls(
    display := "flex",
    flexWrap := "nowrap",
    flexDirection := "row",
    alignItems := "center",
    justifyContent := "space-between",
    alignContent := "space-between",
    padding := "0.5em"
  )

  lazy val linedContentLeft: Cls = cls(
    display := "flex",
    flexWrap := "nowrap",
    justifyContent := "flex-start",
    alignItems := "center",
    width := "22%"
  )

  lazy val linedContentRight: Cls = cls(
    display := "flex",
    flexWrap := "nowrap",
    justifyContent := "flex-end",
    alignItems := "center",
    width := "58%"
  )

  //
  // MARKER CSS
  //

  lazy val marker: Cls = cls(
    display := "inline-block",
    position := "relative",
    padding := "1em",
    background := "#3B3E43",
    color := "#ECF0F1",
    textAlign := "center",
    textTransform := "uppercase",
    whiteSpace := "nowrap",

    &.pseudoExtend(":after")(
      position := "absolute",
      top := "calc(50% - 1.59em)",
      left := "100%",

      afterContent := "''",

      width := "0px",
      height := "0px",
      background := "transparent",

      borderLeft := "1.59em solid #3B3E43",
      borderTop := "1.59em solid transparent",
      borderBottom := "1.59em solid transparent",
      clear := "both"
    )
  )

  //
  // TEXTBOX CSS
  //

  lazy val textbox: Cls = cls(
    display := "inline-block",
    background := "#ECF0F1",
    color := "#3B3E43",
    border := "1px solid #979797",
    borderRadius := "8px",
    overflow := "auto",
    padding := "1em 0.5em 1em"
  )
}
