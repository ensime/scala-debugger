package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for front page.
 */
object FrontPageStyle extends CascadingStyleSheet {
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
    backgroundColor := Colors.EnsimeNormalRed,
    a(
      color := Colors.EnsimeBlack,
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
    color := Colors.EnsimeBlack,
    paddingBottom := "13px",
    margin := "0 1.5em",
    fontSize := "20px"
  )

  val splash = cls(
    padding := "1em",
    backgroundColor := Colors.EnsimeNormalRed,
    color := "white",
    textAlign := "center"
  )

  val getStarted = cls(
    a(
      backgroundColor := Colors.EnsimePaleRed,
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
    borderTop := Colors.FooterBorderLine,
    padding := "2em 0",
    color := Colors.FooterTextGrey,
    a(
      color := Colors.FooterTextGrey,
      textDecoration := "underline",
      cursor := "pointer"
    )
  )
}
