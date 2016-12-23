package org.scaladebugger.docs.layouts.partials

import scalatags.Text.all._

/**
 * Generates front page content.
 */
object FrontPageContent {
  def apply(content: Modifier*): Modifier = {
    div(content)
  }
}
