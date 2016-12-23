package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates front page menu.
 */
object FrontPageMenu {
  def apply(menuItems: MenuItem*): Modifier = {
    // "Item name" -> "Item link"
    @inline def toListItem(menuItem: MenuItem): Modifier =
      li(FrontPageStyle.menuItem, a(href := menuItem.link)(menuItem.name))

    ul(FrontPageStyle.menu)(menuItems.map(toListItem))
  }
}
