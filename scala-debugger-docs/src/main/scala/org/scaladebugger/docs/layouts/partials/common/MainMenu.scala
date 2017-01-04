package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates main menu.
 */
object MainMenu {
  def apply(menuItems: MenuItem*): Modifier = {
    @inline def toListItem(menuItem: MenuItem): Modifier = {
      li(
        a(href := menuItem.link)(menuItem.name)
      )
    }

    tag("nav")(MainNavStyle.navLinks)(
      ul(menuItems.map(toListItem))
    )
  }
}
