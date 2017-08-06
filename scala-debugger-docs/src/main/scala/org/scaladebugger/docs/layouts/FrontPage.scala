package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.styles.{FrontPageStyle, PageStyle}

import scalatags.Text.all._
import org.scaladebugger.docs.styles.Implicits._
import org.senkbeil.grus.layouts.Context

/**
 * Represents the layout for the front page of the site.
 */
class FrontPage extends SitePage(syntaxHighlightTheme = "default") {
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
      |myFunc := func(a, b) {
      |  a + b
      |}
      |
      |result := myFunc 3 9
      |
      |print("Result is " ++ result)
    """.stripMargin


  override protected def preHeadContent(context: Context): Seq[Modifier] = {
    super.preHeadContent(context) ++ Seq(
      FrontPageStyle.global.toStyleTag,
      FrontPageStyle.toStyleTag
    )
  }

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
          span(FrontPageStyle.inlineButtonContainer)(
            Button(
              "Learn More",
              context.mainMenuItems
                .find(_.name.toLowerCase == "about")
                .flatMap(_.link)
                .getOrElse(throw new RuntimeException("Missing about section!")),
              PageStyle.buttonMargin
            ),
            Button(
              "Source Code",
              "https://www.github.com/ensime/scala-debugger",
              PageStyle.buttonMargin
            ),
            Button(
              "Community",
              "https://www.gitter.im/ensime/scala-debugger",
              PageStyle.buttonMargin
            )
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
            /*Tabs.Tab(
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
            ),*/

            // sbt plugin
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
            /*Tabs.Tab.NoInner(
              name = "visual debugger",
              Video("/videos/examples/", "visual-debugger")
            ),*/

            // sbt plugin
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
