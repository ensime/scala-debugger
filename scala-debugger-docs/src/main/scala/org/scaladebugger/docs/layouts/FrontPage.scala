package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.styles.{FrontPageStyle, PageStyle}

import scalatags.Text.all._
import org.scaladebugger.docs.styles.Implicits._

/**
 * Represents the layout for the front page of the site.
 */
class FrontPage extends Page(
  postHeadContent = Seq(FrontPageStyle.global.toStyleTag)
) {
  private val ApiExampleCode =
    s"""
      |val fileName = "file.scala"
      |val lineNumber = 37
      |
      |scalaVirtualMachine.getOrCreateBreakpointRequest(
      |  fileName,
      |  lineNumber
      |).foreach(breakpointEvent => {
      |  val f = breakpointEvent.fileName
      |  val l = breakpointEvent.lineNumber
      |
      |  println(s"Reached breakpoint $$f:$$l")
      |})
    """.stripMargin

  private val LanguageExampleCode =
    """
      |TODO: Implement language example here
    """.stripMargin

  /**
   * Renders the front page.
   *
   * @param content Unused
   * @return The rendered content
   */
  override def render(content: Seq[Modifier] = Nil): Modifier = {
    super.render(Seq(div(
      tag("section")(PageStyle.section, PageStyle.sectionLight)(
        div(PageStyle.sectionContent)(
          h1(PageStyle.heroTitle)(
            EnsimeLogo(),
            span("Scala Debugger")
          ),
          span(PageStyle.heroSubtitle)(
            "Scala abstractions and tooling around the Java Debugger Interface."
          ),
          div(PageStyle.buttonCls)(
            a(href := "/about")("Learn More")
          )
        )
      ),
      tag("section")(
        PageStyle.section,
        PageStyle.sectionDark
      )(
        div(PageStyle.sectionContent, height := "550px")(
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
                """.stripMargin, fitContainer = true, trim = true)
              ),
              LinedContent("sbt plugin", ScalaCodeBlock(
                """
                  |addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0")
                """.stripMargin, fitContainer = true, trim = true)
              )
            ),

            // Language
            Tabs.Tab(
              name = "language",
              LinedContent("sbt", ScalaCodeBlock(
                """
                  |libraryDependencies += "org.scala-debugger" %% "scala-debugger-language" % "1.1.0-M3"
                """.stripMargin, fitContainer = true, trim = true)
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
                """.stripMargin, fitContainer = true, trim = true)
              ),
              LinedContent("run", ScalaCodeBlock("sbt sdb:run",
                fitContainer = true, trim = true))
            )
          )
        )
      ),
      tag("section")(
        PageStyle.section,
        PageStyle.sectionLight
      )(
        div(PageStyle.sectionContent, height := "550px")(
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
                ScalaCodeBlock(ApiExampleCode,
                  fitContainer = true, trim = true)
              )
            ),

            // Language
            Tabs.Tab(
              name = "language",
              LinedContent.Raw(
                ScalaCodeBlock(LanguageExampleCode,
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
    )))
  }
}
