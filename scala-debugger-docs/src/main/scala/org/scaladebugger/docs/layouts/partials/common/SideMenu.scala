package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.SidebarNavStyle
import org.senkbeil.grus.structures.MenuItem

import scalatags.Text.all._

/**
 * Generates the side menu.
 */
object SideMenu {
  private val menuItemBefore = Nil //Seq(":: ", "** ", "... ")
  private val menuItemAfter = Nil //Seq(" ::", "", "")

  def apply(menuItems: MenuItem*): Modifier = {
    @inline def toListItem(menuItem: MenuItem, depth: Int = 0): Modifier = {
      val selectedStyle =
        if (menuItem.selected) Some(SidebarNavStyle.selectedNavLink)
        else None

      val isSelected = menuItem.isDirectlyOrIndirectlySelected
      val selectedAttr = if (isSelected) Some(attr("open").empty) else None

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

      li(
        tag("details")(selectedAttr)(
          tag("summary")(SidebarNavStyle.summary)(
            menuItem.link.map(l =>
              a(selectedStyle, SidebarNavStyle.navLink, href := l)(title)
            ).getOrElse(
              span(selectedStyle, SidebarNavStyle.navLink)(title)
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
