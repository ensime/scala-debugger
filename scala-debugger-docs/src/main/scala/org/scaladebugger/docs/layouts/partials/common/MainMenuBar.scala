package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates the main menu bar.
 */
object MainMenuBar {
  def apply(content: Modifier*): Modifier = {
    div(MainNavStyle.navbar)(content)
  }
}
