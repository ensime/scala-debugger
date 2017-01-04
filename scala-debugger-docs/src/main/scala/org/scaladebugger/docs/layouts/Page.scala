package org.scaladebugger.docs.layouts

import java.net.URL

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.layouts.partials.common.vendor.{ClipboardJS, FontAwesome}
import org.scaladebugger.docs.styles.{MainNavStyle, PageStyle, TabsStyle}

import scalatags.Text.all._

/**
 * Represents the layout for a common site page.
 *
 * @param preHeadContent Content to be added at the beginning of the <head>
 * @param postHeadContent Content to be added at the end of the <head>
 * @param preBodyContent Content to be added at the beginning of the <body>
 * @param postBodyContent Content to be added at the end of the <body>
 * @param htmlModifiers Modifiers to apply on the <html> tag
 * @param bodyModifiers Modifiers to apply on the <body> tag
 * @param selectedMenuItems Will mark each menu item whose name is provided
 *                          as selected
 */
class Page(
  val preHeadContent: Seq[Modifier] = Nil,
  val postHeadContent: Seq[Modifier] = Nil,
  val preBodyContent: Seq[Modifier] = Nil,
  val postBodyContent: Seq[Modifier] = Nil,
  val htmlModifiers: Seq[Modifier] = Nil,
  val bodyModifiers: Seq[Modifier] = Nil,
  val selectedMenuItems: Seq[String] = Nil
) extends Layout {
  import org.scaladebugger.docs.styles.Implicits._
  private lazy val headContent =
    preHeadContent ++
    Seq(
      meta(charset := "utf-8"),
      FontAwesome(),
      PageStyle.global.toStyleTag,
      PageStyle.styleSheetText.toStyleTag,
      MainNavStyle.styleSheetText.toStyleTag,
      TabsStyle.styleSheetText.toStyleTag
    ) ++
    postHeadContent

  private lazy val bodyContent = (content: Seq[Modifier]) =>
    preBodyContent ++
    Seq(
      Header(
        MainMenuBar(
          MainMenuLogo(),
          MainMenu(
            MenuItem(
              name = "Api",
              link = "/docs/api",
              selected = isMenuItemSelected("Api")
            ),
            MenuItem(
              name = "Language",
              link = "/docs/language",
              selected = isMenuItemSelected("Language")
            ),
            MenuItem(
              name = "Sdb",
              link = "/docs/sdb",
              selected = isMenuItemSelected("Sdb")
            ),
            MenuItem(
              name = "Visual Debugger",
              link = "/docs/visual_debugger",
              selected = isMenuItemSelected("Visual Debugger")
            ),
            MenuItem(
              name = "Sbt Plugin",
              link = "/docs/sbt_plugin",
              selected = isMenuItemSelected("Sbt Plugin")
            ),
            MenuItem(
              name = "About",
              link = "/about",
              selected = isMenuItemSelected("About")
            )
          )
        )
      ),
      PageContent(content),
      Footer(
        authorName = "Chip Senkbeil",
        authorUrl = new URL("https://chipsenkbeil.com/"),
        startYear = 2015
      ),
      ClipboardJS()
    ) ++
    postBodyContent

  @inline private def isMenuItemSelected(name: String): Boolean =
    selectedMenuItems.map(_.toLowerCase).contains(name)

  /**
   * Renders a generic page.
   *
   * @param content The content to render as HTML using this layout
   * @return The rendered content
   */
  override def render(content: Seq[Modifier] = Nil): Modifier = {
    html(htmlModifiers: _*)(
      head(headContent: _*),
      body(bodyModifiers: _*)(bodyContent(content))
    )
  }
}
