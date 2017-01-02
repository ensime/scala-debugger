package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.extras.Images
import org.scaladebugger.docs.layouts.partials._
import org.scaladebugger.docs.styles.{FrontPageStyle, MainNavStyle, TabsStyle}

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
        tag("style")(FrontPageStyle.styleSheetText),
        tag("style")(MainNavStyle.styleSheetText),
        tag("style")(TabsStyle.styleSheetText)
      ),
      body()(
        FrontPageHeader(
          FrontPageMenuBar(
            FrontPageLogo(),
            FrontPageMenu(
              MenuItem(name = "Api", link = "/docs/api"),
              MenuItem(name = "Sdb", link = "/docs/sdb"),
              MenuItem(name = "Sbt", link = "/docs/sbt"),
              MenuItem(name = "Visual Debugger", link = "/docs/visual_debugger"),
              MenuItem(name = "About", link = "/about")
            )
          )
        ),
        FrontPageContent(
          tag("section")(FrontPageStyle.section, FrontPageStyle.sectionLight)(
            div(FrontPageStyle.sectionContent)(
              h1(FrontPageStyle.heroTitle)(
                EnsimeLogo(),
                span("Scala Debugger")
              ),
              span(FrontPageStyle.heroSubtitle)(
                "Scala abstractions and tooling around the Java Debugger Interface."
              ),
              div(FrontPageStyle.buttonCls)(
                a(href := "/about")("Learn More")
              )
            )
          ),
          tag("section")(FrontPageStyle.section, FrontPageStyle.sectionDark)(
            div(FrontPageStyle.sectionContent)(

            )
          ),
          tag("section")(FrontPageStyle.section, FrontPageStyle.sectionLight)(
            div(FrontPageStyle.sectionContent)(

            )
          )
        ),
        FrontPageFooter()
      )
    )
  }
}
