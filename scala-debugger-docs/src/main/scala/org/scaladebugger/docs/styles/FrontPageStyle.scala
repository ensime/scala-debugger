package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for front page.
 */
object FrontPageStyle extends CascadingStyleSheet {
  private val PrimaryBackgroundColor = "#60B5CC"
  private val PrimaryForegroundColor = "#34495E"

  // Affects everything but html, to be placed on html
  val global = cls(
    body(margin := 0),
    a(textDecoration := "none"),
    h1(
      marginTop := "1.2em",
      marginBottom := "0.8em",
      fontWeight := "normal"
    )
  )

  val tabs = cls(
    padding := "1em",
    backgroundColor := PrimaryBackgroundColor,
    a(
      color := PrimaryForegroundColor,
      textDecoration := "none"
    ),
    ul(
      margin := 0,
      padding := 0,
      listStyleType := "none",
      textAlign := "center"
    ),
    li(
      display := "inline-block"
    )
  )

  val tab = cls(
    textDecoration := "none",
    color := PrimaryForegroundColor,
    paddingBottom := "13px",
    margin := "0 1.5em",
    fontSize := "20px"
  )

  val splash = cls(
    padding := "1em",
    backgroundColor := PrimaryBackgroundColor,
    color := "white",
    textAlign := "center"
  )

  val getStarted = cls(
    a(
      backgroundColor := PrimaryForegroundColor,
      borderRadius := "6px",
      minWidth := "120px",
      padding := "12px 12px",
      margin := "12px",
      display := "inline-block",
      color := "white",
      fontSize := "1.5em"
    )
  )

  val homeSection = cls(
    h1(
      textAlign := "center",
      fontSize := "3em",
      marginTop := "100px",
      marginBottom := "0px"
    )
  )

  val footerCls = cls(
    textAlign := "center",
    marginTop := "6em",
    borderTop := "1px solid #eeeeee",
    padding := "2em 0",
    color := "#bbbbbb",
    a(
      color := "#bbbbbb",
      textDecoration := "underline",
      cursor := "pointer"
    )
  )
}
