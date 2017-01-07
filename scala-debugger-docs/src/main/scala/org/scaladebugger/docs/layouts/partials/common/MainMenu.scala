package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.structures.MenuItem
import org.scaladebugger.docs.styles.TopbarNavStyle

import scalatags.Text.all._

/**
 * Generates main menu.
 */
object MainMenu {
  def apply(menuItems: MenuItem*): Modifier = {
    @inline def toListItem(menuItem: MenuItem): Modifier = {
      val selectedStyle =
        if (menuItem.selected) Some(TopbarNavStyle.selectedNavLink)
        else None

      li(selectedStyle)(
        menuItem.link.map(l =>
          a(TopbarNavStyle.navLink)(href := l)(menuItem.name)
        ).getOrElse(
          span(TopbarNavStyle.navLink)(menuItem.name)
        )
      )
    }

    tag("nav")(TopbarNavStyle.navLinks)(
      ul(menuItems.map(toListItem))
    )
  }
}
