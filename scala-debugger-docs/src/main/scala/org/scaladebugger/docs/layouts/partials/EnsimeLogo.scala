package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.extras.Images
import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates an image of the Ensime logo.
 */
object EnsimeLogo {
  private val embeddedSrc = Images.inlineEnsimeLogoNoText
  private val urlSrc = "/img/ensime-logo-no-text.svg"

  def apply(): Modifier = {
    img(
      src := embeddedSrc,
      alt := "Ensime Logo"
    )
  }
}
