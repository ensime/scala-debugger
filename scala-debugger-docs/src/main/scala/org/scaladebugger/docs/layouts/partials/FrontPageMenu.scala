package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates front page menu.
 */
object FrontPageMenu {
  lazy val MenuItems = Map(
    "examples" -> "/examples"
  )

  def apply(): Modifier = {
    // "Item name" -> "Item link"
    @inline def toListItem(t: (String, String)): Modifier =
      li(a(FrontPageStyle.tab, href := t._2)(t._1))

    ul(MenuItems.map(toListItem).toSeq)
  }
}
