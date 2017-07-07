package org.scaladebugger.docs.layouts

import java.net.URL

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.layouts.partials.common.vendor._
import org.scaladebugger.docs.styles.{PageStyle, TabsStyle, TopbarNavStyle}
import org.senkbeil.sitegen.layouts.{Context, Page}

import scalatags.Text.all._
import org.scaladebugger.docs.styles.Implicits._

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
abstract class SitePage(
  override val preHeadContent: (Context) => Seq[Modifier] = _ => Nil,
  override val postHeadContent: (Context) => Seq[Modifier] = _ => Nil,
  override val preBodyContent: (Context) => Seq[Modifier] = _ => Nil,
  override val postBodyContent: (Context) => Seq[Modifier] = _ => Nil,
  override val htmlModifiers: (Context) => Seq[Modifier] = _ => Nil,
  override val bodyModifiers: (Context) => Seq[Modifier] = _ => Nil,
  val selectedMenuItems: Seq[String] = Nil,
  val syntaxHighlightTheme: String = "agate"
) extends Page(
  preHeadContent = c => preHeadContent(c) ++ Seq(
    FontAwesomeCSS(),
    HighlightCSS(theme = syntaxHighlightTheme),
    PageStyle.styleSheetText.toStyleTag,
    TopbarNavStyle.styleSheetText.toStyleTag,
    TabsStyle.styleSheetText.toStyleTag
  ),
  postHeadContent = c => postHeadContent(c),
  preBodyContent = c => preBodyContent(c) ++ Seq(
    Header(
      MainMenuBar(
        MainMenuLogo(),
        MainMenu(c.mainMenuItems: _*)
      )
    )
  ),
  postBodyContent = c => Seq(
    Footer(
      authorName = "Chip Senkbeil",
      authorUrl = new URL("https://chipsenkbeil.com/"),
      startYear = 2015
    ),
    ClipboardJS(),
    HighlightJS(),
    ClipboardJSInit(),
    HighlightJSInit()
  ) ++ postBodyContent(c),
  htmlModifiers = c => htmlModifiers(c),
  bodyModifiers = c => bodyModifiers(c)
)
