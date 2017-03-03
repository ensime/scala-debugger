package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for all pages.
 */
object PageStyle extends CascadingStyleSheet {
  initStyleSheet()

  import scalatags.Text.styles2.{content => afterContent}

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
    background := "#3B3E43",
    color := "#ECF0F1",
    padding := "1em 1.5em",
    borderRadius := "8px",
    overflow := "hidden",
    textOverflow := "ellipsis",
    whiteSpace := "nowrap",
    textDecoration := "none",

    a(
      color := "#ECF0F1",
      textDecoration := "none"
    )
  )

  lazy val buttonMargin: Cls = cls(
    margin := "1em 1em"
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
    padding := "1em 0.5em 1em",

    Selector(".hljs")(
      background := "#ECF0F1",
      color := "#3B3E43"
    )
  )

  //
  // COPY BUTTON CSS
  //

  lazy val copyContainer: Cls = cls(
    position := "relative",

    &.hover(
      copyButton(
        opacity := "1"
      )
    )
  )

  lazy val copyButton: Cls = cls(
    display := "inline-block",
    cursor := "pointer",

    position := "absolute",
    top := "0",
    right := "0",

    fontSize := "1em",
    padding := "0.5em 1em",

    textDecoration := "none",
    border := "transparent",
    background := "#EF4551",
    color := "#ECF0F1",
    borderRadius := "8px",

    opacity := "0",
    transition := "opacity 0.35s ease-in-out",

    &.active(
      background := "#A84444"
    )
  )
}
