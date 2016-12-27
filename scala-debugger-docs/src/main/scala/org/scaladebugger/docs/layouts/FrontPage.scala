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
              MenuItem(name = "Api", children = Seq(
                MenuItem(name = "Installing & Getting Started"),
                MenuItem(name = "Advanced Topics"),
                MenuItem(name = "Cookbook")
              )),
              MenuItem(name = "Sdb", children = Seq(
                MenuItem(name = "Installing & Getting Started"),
                MenuItem(name = "Methods"),
                MenuItem(name = "CLI Options")
              )),
              MenuItem(name = "Sbt", children = Seq(
                MenuItem(name = "Installing & Getting Started")
              )),
              MenuItem(name = "Visual Debugger", children = Seq(
                MenuItem(name = "Installing & Getting Started")
              )),
              MenuItem(name = "About", children = Seq(
                MenuItem(name = "Roadmap"),
                MenuItem(name = "Release Notes"),
                MenuItem(name = "Contributing"),
                MenuItem(name = "License")
              ))
            )
          )
        ),
        FrontPageContent(),
        FrontPageFooter()
      )
    )
  }
}
