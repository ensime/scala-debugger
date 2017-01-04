package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.styles.{PageStyle, SidebarNavStyle}
import org.scaladebugger.docs.styles.Implicits._

import scalatags.Text.all._

/**
 * Represents the layout for the front page of the site.
 */
class DocPage extends Page(
  postHeadContent = Seq(SidebarNavStyle.styleSheetText.toStyleTag)
) {
  /**
   * Renders a page of documentation.
   *
   * @param content The documentation page contents
   * @return The rendered content
   */
  override def render(content: Seq[Modifier] = Nil): Modifier = {
    super.render(Seq(
      div()(),
      div()(content: _*)
    ))
  }
}