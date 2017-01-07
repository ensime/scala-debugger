package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.structures.MenuItem
import org.scaladebugger.docs.styles.SidebarNavStyle

import scalatags.Text.all._

/**
 * Generates the side menu.
 */
object SideMenu {
  private val menuItemBefore = Seq(":: ", "** ", "... ")
  private val menuItemAfter = Seq(" ::", "", "")

  def apply(menuItems: MenuItem*): Modifier = {
    @inline def toListItem(menuItem: MenuItem, depth: Int = 0): Modifier = {
      val selectedStyle =
        if (menuItem.selected) Some(SidebarNavStyle.selectedNavLink)
        else None

      val childrenMenu =
        if (menuItem.children.nonEmpty)
          Some(toMenu(menuItem.children, depth + 1))
        else
          None

      val prefix =
        if (depth < menuItemBefore.length) Some(raw(menuItemBefore(depth)))
        else None
      val suffix =
        if (depth < menuItemAfter.length) Some(raw(menuItemAfter(depth)))
        else None
      val title = prefix.toSeq ++ Seq(raw(menuItem.name)) ++ suffix.toSeq ++
        childrenMenu.map(_ => span(SidebarNavStyle.summaryExpandIcon)()).toSeq

      li(selectedStyle)(
        tag("details")(
          tag("summary")(SidebarNavStyle.summary)(
            menuItem.link.map(l =>
              a(SidebarNavStyle.navLink, href := l)(title)
            ).getOrElse(
              span(SidebarNavStyle.navLink)(title)
            )
          ),
          childrenMenu
        )
      )
    }

    @inline def toMenu(menuItems: Seq[MenuItem], depth: Int = 0): Modifier = {
      ul(menuItems.map(mi => toListItem(mi, depth)))
    }

    toMenu(menuItems)
  }
}
