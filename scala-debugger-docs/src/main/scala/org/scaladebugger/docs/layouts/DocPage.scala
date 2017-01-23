package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.layouts.partials.common.SideMenu
import org.scaladebugger.docs.styles.Implicits._
import org.scaladebugger.docs.styles.{DocPageStyle, SidebarNavStyle}

import scalatags.Text.all._

/**
 * Represents the layout for the front page of the site.
 */
class DocPage extends Page(
  postHeadContent = Seq(
    DocPageStyle.global.toStyleTag,
    DocPageStyle.styleSheetText.toStyleTag,
    SidebarNavStyle.global.toStyleTag,
    SidebarNavStyle.styleSheetText.toStyleTag
  ),
  bodyModifiers = Seq(DocPageStyle.bodyCls)
) {
  /**
   * Renders a page of documentation.
   *
   * @param content The documentation page contents
   * @return The rendered content
   */
  override def render(content: Seq[Modifier] = Nil): Modifier = {
    super.render(Seq(div(
      flex := "1 1 auto",
      minHeight := "2em",
      overflow := "auto",
      display := "flex"
    )(
      tag("nav")(SidebarNavStyle.navbar, SidebarNavStyle.navLinks, flex := "0 0 auto")(
        SideMenu(context.sideMenuItems: _*)
      ),
      div(DocPageStyle.mainContent, flex := "1 1 auto")(
        div(DocPageStyle.viewArea)(content: _*)
      )
    )))
  }
}
