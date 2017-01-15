package org.scaladebugger.docs.layouts.partials.common.vendor

import scalatags.Text.all._

/**
 * Represents a <link ... > containing highlight default.css.
 */
object HighlightCSS {
  /**
   * Creates highlight css link tag.
   *
   * @param theme The highlight.js theme to use
   * @return The link tag
   */
  def apply(theme: String): Modifier = {
    link(rel := "stylesheet", href := s"/styles/vendor/highlight/$theme.css")
  }
}
