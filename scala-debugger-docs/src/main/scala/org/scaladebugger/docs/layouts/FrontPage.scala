package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.extras.Images
import org.scaladebugger.docs.layouts.partials._
import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Represents the layout for the front page of the site.
 */
object FrontPage extends Layout {
  override def render(content: Seq[Modifier]): Modifier = {
    html(FrontPageStyle.global)(
      head(
        meta(charset := "utf-8"),
        FontAwesome(),
        tag("style")(FrontPageStyle.styleSheetText)
      ),
      body()(
        FrontPageHeader(
          FrontPageMenuBar(
            FrontPageLogo(),
            FrontPageMenu(
              MenuItem(name = "Products"),
              MenuItem(name = "Why Fitbit"),
              MenuItem(name = "Get Motivated"),
              MenuItem(name = "App & Dashboard"),
              MenuItem(name = "Help")
            )
          )
        ),
        FrontPageContent(),
        FrontPageFooter()
      )
    )
  }
}
