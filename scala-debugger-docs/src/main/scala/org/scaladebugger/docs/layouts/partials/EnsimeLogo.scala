package org.scaladebugger.docs.layouts.partials

import scalatags.Text.all._

/**
 * Generates an image of the Ensime logo.
 */
object EnsimeLogo {
  private val urlSrc = "/img/ensime-logo-no-text.svg"

  def apply(): Modifier = {
    img(
      src := urlSrc,
      alt := "Ensime Logo"
    )
  }
}
