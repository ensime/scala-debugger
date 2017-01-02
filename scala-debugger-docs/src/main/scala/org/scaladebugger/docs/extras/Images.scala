package org.scaladebugger.docs.extras

import org.scaladebugger.docs.ImageLoader

/**
 * Contains images from resources.
 */
object Images {
  lazy val ensimeLogoSvg: String = ImageLoader.imageBase64("img/ensime-logo.svg")
  lazy val ensimeLogoNoTextSvg: String = ImageLoader.imageBase64("img/ensime-logo-no-text.svg")

  /** Ensime Logo - to be fed into src */
  lazy val inlineEnsimeLogo: String = "data:image/svg+xml;charset=utf8;base64," + ensimeLogoSvg

  /** Ensime Logo without ENSIME text - to be fed into src */
  lazy val inlineEnsimeLogoNoText: String = "data:image/svg+xml;charset=utf8;base64," + ensimeLogoNoTextSvg
}
