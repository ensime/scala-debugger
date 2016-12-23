package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for front page.
 */
object FrontPageStyle extends CascadingStyleSheet {
  import scalatags.Text.styles2.{content => afterContent}

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

  val menuBar = cls(
    display := flex.cssName,
    alignItems := "center"
  )

  /**
   * Horizontal list.
   */
  val menu = cls(
    display := "inline-block",
    listStyle := "none",
    padding := "0",
    margin := "0",
    verticalAlign := "baseline",
    color := Colors.MenuItemTextColor
  )

  val menuItem = cls(
    display := "block",
    float := "left",
    padding := "0 0.6em",
    lineHeight := "60px",
    fontFamily := "'Proxima Nova Regular', Arial, sans-serif",
    fontSize := "16px",
    a(
      color := "#626161",
      textDecoration := "none"
    ),
    &.pseudoExtend(":after")(
      display := "inline-block",
      afterContent := "'\\f107'",
      opacity := 0.5,
      color := "#102429",
      font := "normal normal normal 14px/1 FontAwesome",
      fontSize := "inherit"
    )
  )

  val headerCls = cls(
    fontSize := "1.6em",
    height := "60px",
    position := "relative",
    margin := "0 auto",
    padding := "0 10px"
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
