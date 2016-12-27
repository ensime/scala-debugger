package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates front page menu.
 */
object FrontPageMenu {
  def apply(menuItems: MenuItem*): Modifier = {
    @inline def toListItem(menuItem: MenuItem): Modifier = {
      // Optionally contains children
      val childrenElement =
        if (menuItem.children.nonEmpty) Some(ul(FrontPageStyle.menuItemExpand)(
          menuItem.children.map(toListItem))
        ) else None
      val elements = Seq(a(href := menuItem.link)(menuItem.name)) ++
        childrenElement.toSeq

      // Optionally show children marker
      val childrenClass =
        if (menuItem.children.nonEmpty) Some(FrontPageStyle.menuItemWithChildren)
        else None
      val classes = Seq(FrontPageStyle.menuItem) ++ childrenClass.toSeq
      li(classes)(elements: _*)
    }

    ul(FrontPageStyle.menu)(menuItems.map(toListItem))
  }
}
