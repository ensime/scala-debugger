package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates front page menu bar.
 */
object FrontPageMenuBar {
  def apply(content: Modifier*): Modifier = {
    div(MainNavStyle.navbar)(content)
  }
}
