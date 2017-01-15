package org.scaladebugger.docs.layouts

import java.net.URL

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.layouts.partials.common.vendor._
import org.scaladebugger.docs.styles.{PageStyle, TabsStyle, TopbarNavStyle}

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
 * @param syntaxHighlightTheme The theme to use for syntax highlighting; themes
 *                             are from the highlight.js list
 */
abstract class Page(
  val preHeadContent: Seq[Modifier] = Nil,
  val postHeadContent: Seq[Modifier] = Nil,
  val preBodyContent: Seq[Modifier] = Nil,
  val postBodyContent: Seq[Modifier] = Nil,
  val htmlModifiers: Seq[Modifier] = Nil,
  val bodyModifiers: Seq[Modifier] = Nil,
  val selectedMenuItems: Seq[String] = Nil,
  val syntaxHighlightTheme: String = "agate"
) extends Layout {
  import org.scaladebugger.docs.styles.Implicits._
  private lazy val headContent =
    preHeadContent ++
    Seq(
      meta(charset := "utf-8"),
      FontAwesomeCSS(),
      HighlightCSS(theme = syntaxHighlightTheme),
      PageStyle.styleSheetText.toStyleTag,
      TopbarNavStyle.styleSheetText.toStyleTag,
      TabsStyle.styleSheetText.toStyleTag
    ) ++
    postHeadContent ++
    context.title.map(t => tag("title")(t))

  private lazy val bodyContent = (content: Seq[Modifier]) =>
    preBodyContent ++
    Seq(
      Header(
        MainMenuBar(
          MainMenuLogo(),
          MainMenu(context.mainMenuItems: _*)
        )
      )
    ) ++
    content ++
    Seq(
      Footer(
        authorName = "Chip Senkbeil",
        authorUrl = new URL("https://chipsenkbeil.com/"),
        startYear = 2015
      ),
      ClipboardJS(),
      HighlightJS(),
      ClipboardJSInit(),
      HighlightJSInit()
    ) ++
    postBodyContent

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
