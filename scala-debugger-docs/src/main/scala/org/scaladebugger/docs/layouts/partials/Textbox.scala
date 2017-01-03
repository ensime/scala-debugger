package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates a textbox.
 */
object Textbox {
  def apply(fitContainer: Boolean, content: Modifier*): Modifier = {
    if (fitContainer)
      span(FrontPageStyle.textbox, FrontPageStyle.fitContainer)(content)
    else
      span(FrontPageStyle.textbox)(content)
  }

  def apply(content: Modifier*): Modifier = apply(false, content: _*)
}
