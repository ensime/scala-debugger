package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates front page menu.
 */
object FrontPageMenu {
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
