package org.scaladebugger.docs.layouts

import java.net.URL
import java.util.Calendar

import org.scaladebugger.docs.layouts.partials.common.SideMenu
import org.scaladebugger.docs.styles.Implicits._
import org.scaladebugger.docs.styles.{DocPageStyle, SidebarNavStyle}

import scalatags.Text.all._

/**
 * Represents the layout for the front page of the site.
 */
class DocPage extends Page(
  postHeadContent = Seq(
    DocPageStyle.styleSheetText.toStyleTag,
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
    super.render(Seq(div(flex := "1 1 auto", minHeight := "100px")(
      div(display := "flex", height := "100%")(
        div(SidebarNavStyle.navbar, flex := "0 0 auto")(
          tag("nav")(SidebarNavStyle.navLinks)(
            SideMenu(context.sideMenuItems: _*)
          ),
          copyright("Chip Senkbeil", new URL("https://chipsenkbeil.com"), 2015)
        ),
        div(DocPageStyle.mainContent, flex := "1 1 auto", maxHeight := "100%")(
          div(
            padding := "2em"
          )(content: _*)
        )
      )
    )))
  }

  private def copyright(
    authorName: String,
    authorUrl: URL,
    startYear: Int
  ): Modifier = {
    span(
      position := "absolute",
      left := "0em",
      bottom := "0em",

      width := "100%",
      textAlign := "center",
      paddingBottom := "1em",

      fontSize := "0.8em",
      fontWeight := "lighter"
    )(
      raw("Copyright "),
      i(`class` := "fa fa-copyright", attr("aria-hidden") := "true"),
      raw(" "),
      a(href := authorUrl.toString)(authorName),
      raw(s", $startYear-${Calendar.getInstance().get(Calendar.YEAR)}")
    )
  }
}
