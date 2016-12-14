package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.ImageLoader

import scalatags.Text.all._

/**
 * Generates a logo for the Scala debugger project.
 */
object FrontPageLogo {
  lazy val AHref = "/"
  lazy val ImgSrc = "data:image/png;base64," + ImageLoader.imageBase64("ensime-logo.png")
  lazy val ImgWidth = "24px"

  def apply(): Modifier = {
    a(href := AHref, position := "absolute", left := "1em", top := "1em")(
      img(src := ImgSrc, width := ImgWidth)()
    )
  }
}
