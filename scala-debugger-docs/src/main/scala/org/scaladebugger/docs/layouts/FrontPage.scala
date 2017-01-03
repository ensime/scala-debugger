package org.scaladebugger.docs.layouts

import java.net.URL

import org.scaladebugger.docs.layouts.partials._
import org.scaladebugger.docs.styles.{FrontPageStyle, MainNavStyle, TabsStyle}

import scalatags.Text.all._

/**
 * Represents the layout for the front page of the site.
 */
object FrontPage extends Layout {
  override def render(content: Seq[Modifier]): Modifier = {
    html(
      head(
        meta(charset := "utf-8"),
        FontAwesome(),
        tag("style")(FrontPageStyle.global),
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
              MenuItem(name = "Language", link = "/docs/language"),
              MenuItem(name = "Sdb", link = "/docs/sdb"),
              MenuItem(name = "Visual Debugger", link = "/docs/visual_debugger"),
              MenuItem(name = "Sbt Plugin", link = "/docs/sbt_plugin"),
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
          tag("section")(
            FrontPageStyle.section,
            FrontPageStyle.sectionDark
          )(
            div(FrontPageStyle.sectionContent, height := "550px")(
              h1(
                i(
                  `class` := "fa fa-laptop",
                  attr("aria-hidden") := "true"
                )(),
                span("Installation")
              ),
              Tabs.Light(
                identifier = "installation",

                // API
                Tabs.Tab(
                  name = "api",
                  LinedContent("sbt", ScalaCodeBlock(
                    """
                      |libraryDependencies += "org.scala-debugger" %% "scala-debugger-api" % "1.1.0-M3"
                    """.stripMargin, trim = true)
                  ),
                  LinedContent("sbt plugin", ScalaCodeBlock(
                    """
                      |addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0")
                    """.stripMargin, trim = true)
                  )
                ),

                // Language
                Tabs.Tab(
                  name = "language",
                  LinedContent("sbt", ScalaCodeBlock(
                    """
                      |libraryDependencies += "org.scala-debugger" %% "scala-debugger-language" % "1.1.0-M3"
                    """.stripMargin, trim = true)
                  )
                ),

                // SDB
                Tabs.Tab(
                  name = "sdb",
                  LinedContent("download jar", Button(
                    name = "sdb 1.1.0-M3 built with Scala 2.10",
                    link = "/downloads/sdb/1.1.0-M3/sdb-2.10.jar"
                  )),
                  LinedContent("download jar", Button(
                    name = "sdb 1.1.0-M3 built with Scala 2.11",
                    link = "/downloads/sdb/1.1.0-M3/sdb-2.11.jar"
                  )),
                  LinedContent("download jar", Button(
                    name = "sdb 1.1.0-M3 built with Scala 2.12",
                    link = "/downloads/sdb/1.1.0-M3/sdb-2.12.jar"
                  ))
                ),

                // Visual Debugger
                Tabs.Tab(
                  name = "visual debugger",
                  LinedContent("download jar", Button(
                    name = "vsdb 1.1.0-M3 built with Scala 2.10",
                    link = "/downloads/vsdb/1.1.0-M3/vsdb-2.10.jar"
                  )),
                  LinedContent("download jar", Button(
                    name = "vsdb 1.1.0-M3 built with Scala 2.11",
                    link = "/downloads/vsdb/1.1.0-M3/vsdb-2.11.jar"
                  )),
                  LinedContent("download jar", Button(
                    name = "vsdb 1.1.0-M3 built with Scala 2.12",
                    link = "/downloads/vsdb/1.1.0-M3/vsdb-2.12.jar"
                  ))
                ),

                // SBT plugin
                Tabs.Tab(
                  name = "sbt",
                  LinedContent("sbt plugin", ScalaCodeBlock(
                    """
                      |addSbtPlugin("org.scala-debugger" % "sbt-scala-debugger" % "1.1.0-M3")
                    """.stripMargin, trim = true)
                  ),
                  LinedContent("run", ScalaCodeBlock("sbt sdb:run"))
                )
              )
            )
          ),
          tag("section")(
            FrontPageStyle.section,
            FrontPageStyle.sectionLight
          )(
            div(FrontPageStyle.sectionContent, height := "550px")(
              h1(
                i(
                  `class` := "fa fa-gears",
                  attr("aria-hidden") := "true"
                )(),
                span("Demos")
              ),

              Tabs.Dark(
                identifier = "demos",

                // API
                Tabs.Tab(
                  name = "api",
                  LinedContent.Raw(
                    ScalaCodeBlock("TODO: Write some code",
                      fitContainer = true, trim = true)
                  )
                ),

                // Language
                Tabs.Tab(
                  name = "language",
                  LinedContent.Raw(
                    ScalaCodeBlock("TODO: Write some code",
                      fitContainer = true, trim = true)
                  )
                ),

                // SDB
                Tabs.Tab.NoInner(
                  name = "sdb",
                  Video("/videos/examples/", "sdb")
                ),

                // Visual Debugger
                Tabs.Tab.NoInner(
                  name = "visual debugger",
                  Video("/videos/examples/", "visual-debugger")
                ),

                // SBT plugin
                Tabs.Tab.NoInner(
                  name = "sbt",
                  Video("/videos/examples/", "sbt-plugin")
                )
              )
            )
          )
        ),
        FrontPageFooter(
          authorName = "Chip Senkbeil",
          authorUrl = new URL("https://chipsenkbeil.com/"),
          startYear = 2015
        )
      )
    )
  }
}
