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
        tag("style")({
          println(FrontPageStyle.styleSheetText)
          FrontPageStyle.styleSheetText
        })
      ),
      body()(
        FrontPageHeader(
          FrontPageLogo(),
          FrontPageMenu(
            MenuItem(name = "test1"),
            MenuItem(name = "test2"),
            MenuItem(name = "test3")
          )
        ),
        FrontPageContent(
          img(src := Images.inlineEnsimeLogoNoText)
        ),
        FrontPageFooter()
      )
    )
  }
}
