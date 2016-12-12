package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.layouts.partials.{FrontPageLogo, FrontPageMenu}
import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Represents the layout for the front page of the site.
 */
object FrontPage extends Layout {
  override def render(content: Seq[Modifier]): Modifier = {
    html(FrontPageStyle.global)(
      head(
        tag("style")(FrontPageStyle.styleSheetText)
      ),
      body()(
        div(
          div(FrontPageStyle.tabs)(
            FrontPageLogo(),
            FrontPageMenu()
          ),
          div(FrontPageStyle.splash)(
            div(fontSize := "120px", padding := "16px 0px")("scala debugger"),
            div(fontSize := "26px", padding := "8px 0px")(
              "API and tooling for Scala developers"
            ),
            div(fontSize := "16px", padding := "8px 0px")(
              "Bringing Scala back to the forefront of the debugging experience"
            ),
            br(),
            div(FrontPageStyle.getStarted)(
              a(href := "/try")("Try Online"),
              a(href := "/install")("Install")
            )
          ),
          tag("section")(FrontPageStyle.homeSection)(
            h1("Features"),
            ul(`class` := "features")(
            )
          ),
          tag("section")(FrontPageStyle.homeSection)(
            h1("Examples"),
            ul(`class` := "examples")(
            )
          ),
          tag("section")(FrontPageStyle.homeSection)(
            h1("Featured Users"),
            ul(`class` := "featured-users")(
            )
          ),
          div(FrontPageStyle.splash, margin := "100px 0px")(
            div(FrontPageStyle.getStarted)(
              a(href := "/try")("Try Online"),
              a(href := "/install")("Install")
            )
          ),
          div(FrontPageStyle.footerCls)(
            raw("""All code for this site is open source. &#8212; &#169; 2015-2016 """),
            a(href := "https://chipsenkbeil.com")("Chip Senkbeil")
          )
        )
      )
    )
  }
}
