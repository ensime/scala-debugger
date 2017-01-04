package org.scaladebugger.docs.layouts.partials.common

import scalatags.Text.all._

/**
 * Generates front page content.
 */
object PageContent {
  def apply(content: Modifier*): Modifier = {
    div(content)
  }
}
