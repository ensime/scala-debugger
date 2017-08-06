package org.scaladebugger.docs.layouts

import java.net.URL

import org.scaladebugger.docs.layouts.partials.common._
import org.scaladebugger.docs.layouts.partials.common.vendor._
import org.scaladebugger.docs.styles.{PageStyle, TabsStyle, TopbarNavStyle}
import org.senkbeil.grus.layouts.{Context, Page}

import scalatags.Text.all._
import org.scaladebugger.docs.styles.Implicits._

/**
 * Represents the layout for a common site page.
 *
 * @param selectedMenuItems Will mark each menu item whose name is provided
 *                          as selected
 * @param syntaxHighlightTheme The theme to use for syntax highlighting; themes
 *                             are from the highlight.js list
 */
abstract class SitePage(
  val selectedMenuItems: Seq[String] = Nil,
  val syntaxHighlightTheme: String = "agate"
) extends Page {
  override protected def preHeadContent(context: Context): Seq[Modifier] =
    super.preHeadContent(context) ++ Seq(
      FontAwesomeCSS(),
      HighlightCSS(theme = syntaxHighlightTheme),
      PageStyle.styleSheetText.toStyleTag,
      TopbarNavStyle.styleSheetText.toStyleTag,
      TabsStyle.styleSheetText.toStyleTag
    )

  override protected def preBodyContent(context: Context): Seq[Modifier] =
    super.preBodyContent(context) ++ Seq(
      Header(
        MainMenuBar(
          MainMenuLogo(),
          MainMenu(context.mainMenuItems: _*)
        )
      )
    )

  override protected def postBodyContent(context: Context): Seq[Modifier] =
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
    ) ++ super.postBodyContent(context)
}
