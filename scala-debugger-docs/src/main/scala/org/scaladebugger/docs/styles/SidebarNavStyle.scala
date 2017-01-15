package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for the side bar.
 */
object SidebarNavStyle extends CascadingStyleSheet {
  import scalatags.Text.styles2.{content => pseudoContent}

  private val expandIconClosed = "\\25B6"
  private val expandIconOpen = "\\25BC"

  lazy val global: String =
    s"""
      |details[open] > summary > .${navLink.name} > .${summaryExpandIcon.name}::after {
      | content: '$expandIconOpen';
      |}
    """.stripMargin

  lazy val navbar: Cls = cls(
    display := "inline-block",

    fontFamily := "'Helvetica Neue', Helvetica, Arial, sans-serif",
    background := "#D8D8D8",
    color := "#3B3E43",

    width := "16em",

    a(
      textDecoration := "none",
      color := "#3B3E43"
    )
  )

  lazy val navLinks: Cls = cls(
    maxHeight := "100%",
    overflowY := "auto",
    padding := "0.5em",
    listStyleType := "none",

    ul(
      paddingLeft := "0em",
      listStyleType := "none",
      navLink(
        fontSize := "1.1em",
        textTransform := "uppercase"
      ),
      li(
        margin := "1.5em 0em",
        ul(
          paddingLeft := "0.6125em",
          navLink(
            fontSize := "1em",
            textTransform := "capitalize"
          ),
          li(
            margin := "1em 0em",
            ul(
              paddingLeft := "1.25em",
              navLink(
                fontSize := "0.9em",
                textTransform := "none"
              )
            )
          )
        )
      )
    ),

    a(
      textDecoration := "none",
      color := "#3B3E43"
    )
  )

  lazy val navLink: Cls = cls(
    display := "inline-block",
    width := "100%",
    &.hover(
      background := "#3B3E43",
      color := "#D8D8D8"
    )
  )

  lazy val selectedNavLink: Cls = cls(
    padding := "0em",
    background := "#EBF0F1",
    borderRadius := "8px",

    a(
      color := "#3B3E43",
      border := "initial"
    )
  )

  lazy val summary: Cls = cls(
    display := "block",
    cursor := "pointer",

    &.pseudoExtend(":-webkit-details-marker")(
      display := "none"
    )
  )

  lazy val summaryExpandIcon: Cls = cls(
    fontSize := "0.8em",
    &.pseudoExtend(":after")(
      pseudoContent := s"'$expandIconClosed'",
      paddingLeft := "1em"
    )
  )
}
