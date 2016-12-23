package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates front page menu bar.
 */
object FrontPageMenuBar {
  def apply(content: Modifier*): Modifier = {
    div(FrontPageStyle.menuBar)(content)
  }
}
