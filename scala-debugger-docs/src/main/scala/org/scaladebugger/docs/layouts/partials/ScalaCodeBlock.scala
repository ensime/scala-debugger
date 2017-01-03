package org.scaladebugger.docs.layouts.partials

import scalatags.Text.all._

/**
 * Generates an HTML code block for Scala.
 */
object ScalaCodeBlock {
  def apply(
    text: String,
    fitContainer: Boolean = false,
    trim: Boolean = false
  ): Modifier = {
    val ttext = if (trim) text.trim else text
    Textbox(fitContainer, pre(code(ttext)))
  }
}
