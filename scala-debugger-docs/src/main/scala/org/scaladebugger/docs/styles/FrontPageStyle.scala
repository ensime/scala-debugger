package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Represents stylesheet for front page.
 */
object FrontPageStyle extends CascadingStyleSheet {
  import scalatags.Text.styles2.{content => afterContent}

  // Affects everything but html, to be placed on html
  lazy val global: Cls = cls(
    body(margin := 0),
    a(textDecoration := "none"),
    h1(
      marginTop := "1.2em",
      marginBottom := "0.8em",
      fontWeight := "normal"
    )
  )

  lazy val menuBar: Cls = cls(
    display := flex.cssName,
    alignItems := "center"
  )

  lazy val menu: Cls = cls(
    display := "inline-block",
    listStyle := "none",
    padding := "0",
    margin := "0",
    verticalAlign := "baseline",
    color := Colors.MenuItemTextColor
  )

  lazy val menuItem: Cls = cls(
    display := "block",
    position := "relative",

    textTransform := "uppercase",
    whiteSpace := "nowrap",
    float := "left",
    padding := "0 0.6em",
    fontFamily := "'Proxima Nova Regular', Arial, sans-serif",
    fontSize := "0.6em",
    a(
      color := "#626161",
      textDecoration := "none",
      &.hover(
        color := "#00afb8"
      )
    ),
    &.hover(
      clsSelector(menuItemExpand)(
        display := "block"
      )
    )
  )

  lazy val menuItemWithChildren: Cls = cls(
    &.pseudoExtend(":after")(
      display := "inline-block",
      afterContent := "'\\f107'",
      opacity := 0.5,
      color := "#102429",
      font := "normal normal normal 14px/1 FontAwesome",
      fontSize := "inherit"
    )
  )

  lazy val menuItemExpand: Cls = cls(
    position := "absolute",
    display := "none",
    border := "1px solid #102429",
    borderRadius := "5px",
    top := "1em",
    left := "0em",

    zIndex := "9999",
    backgroundColor := "#fff",
    lineHeight := "1.1em",
    margin := "0",
    padding := "0",
    verticalAlign := "baseline"
  )

  lazy val headerCls: Cls = cls(
    fontSize := "1.6em",
    height := "60px",
    position := "relative",
    margin := "0 auto",
    padding := "0 10px"
  )

  lazy val footerCls: Cls = cls(
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
