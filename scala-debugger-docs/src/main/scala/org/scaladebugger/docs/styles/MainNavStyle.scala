package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for main navigation menu (top bar).
 */
object MainNavStyle extends CascadingStyleSheet {
  lazy val headerCls: Cls = cls(
    width := "100%",
    height := "auto"
  )

  lazy val navbar: Cls = cls(
    display := "flex",
    flexDirection := "row",
    flexWrap := "nowrap",
    justifyContent := "space-between",
    alignItems := "center",
    padding := "0.1em 1em",
    fontSize := "1.5em"
  )

  lazy val navLogo: Cls = cls(
    display := "flex",
    flexDirection := "row",
    flexWrap := "nowrap",
    justifyContent := "flex-start",
    alignItems := "center",
    width := "25%",

    img(
      height := "3em",
      paddingRight := "0.5em"
    ),

    span(
      fontSize := "1em",
      whiteSpace := "nowrap"
    )
  )

  lazy val navLinks: Cls = cls(
    width := "70%",

    ul(
      display := "flex",
      flexDirection := "row",
      flexWrap := "nowrap",
      justifyContent := "flex-start",
      listStyleType := "none"
    ),

    li(
      padding := "0em 0.5em"
    ),

    a(
      color := "#EBF0F1",
      fontSize := "0.65em",
      textDecoration := "none",
      textTransform := "uppercase",
      borderBottom := "1px solid #EBF0F1",
      whiteSpace := "nowrap",
      overflow := "hidden",
      textOverflow := "ellipsis"
    )
  )
}
