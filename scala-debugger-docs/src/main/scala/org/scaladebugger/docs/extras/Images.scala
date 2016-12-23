package org.scaladebugger.docs.extras

import org.scaladebugger.docs.ImageLoader

/**
 * Contains images from resources.
 */
object Images {
  lazy val ensimeLogoSvg = ImageLoader.imageBase64("ensime-logo.svg")
  lazy val ensimeLogoNoTextSvg = ImageLoader.imageBase64("ensime-logo-no-text.svg")

  /** Ensime Logo - to be fed into src */
  lazy val inlineEnsimeLogo = "data:image/svg+xml;charset=utf8;base64," + ensimeLogoSvg

  /** Ensime Logo without ENSIME text - to be fed into src */
  lazy val inlineEnsimeLogoNoText = "data:image/svg+xml;charset=utf8;base64," + ensimeLogoNoTextSvg
}
