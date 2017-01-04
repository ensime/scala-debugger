package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._

/**
 * Generates a textbox.
 */
object Textbox {
  def apply(fitContainer: Boolean, content: Modifier*): Modifier = {
    if (fitContainer)
      span(PageStyle.textbox, PageStyle.fitContainer)(content)
    else
      span(PageStyle.textbox)(content)
  }

  def apply(content: Modifier*): Modifier = apply(false, content: _*)
}
