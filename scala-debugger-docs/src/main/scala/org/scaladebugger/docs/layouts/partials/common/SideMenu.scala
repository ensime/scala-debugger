package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.SidebarNavStyle

import scalatags.Text.all._

/**
 * Generates the side menu.
 */
object SideMenu {
  def apply(menuItems: MenuItem*): Modifier = {
    @inline def toListItem(menuItem: MenuItem): Modifier = {
      val selectedStyle =
        if (menuItem.selected) Some(SidebarNavStyle.selectedNavLink)
        else None

      val childrenMenu =
        if (menuItem.children.nonEmpty) Some(SideMenu(menuItem.children: _*))
        else None

      li(selectedStyle)(
        tag("details")(
          tag("summary")(SidebarNavStyle.summary)(
            menuItem.link.map(l =>
              a(SidebarNavStyle.navLink, href := l)(menuItem.name)
            ).getOrElse(
              span(SidebarNavStyle.navLink)(menuItem.name)
            )
          ),
          childrenMenu
        )
      )
    }

    ul(menuItems.map(toListItem))
  }
}
