package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._

/**
 * Generates a textbox.
 */
object Textbox {
  def apply(fitContainer: Boolean, content: Modifier*): Modifier = {
    val fitContainerStyle =
      if (fitContainer) Some(PageStyle.fitContainer)
      else None

    span(PageStyle.textbox, fitContainerStyle)(content)
  }

  def apply(content: Modifier*): Modifier = apply(false, content: _*)
}
