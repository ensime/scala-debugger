package org.scaladebugger.docs.extras

import org.scaladebugger.docs.ImageLoader

/**
 * Contains images from resources.
 */
object Images {
  /** Ensime Logo - to be fed into src */
  lazy val inlineEnsimeLogo =
    "data:image/svg+xml;utf8," + ImageLoader.imageText("ensime-logo.svg")

  /** Ensime Logo without ENSIME text - to be fed into src */
  lazy val inlineEnsimeLogoNoText =
    "data:image/svg+xml;utf8," + ImageLoader.imageText("ensime-logo-no-text.svg")
}
