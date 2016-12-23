package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.extras.Images

import scalatags.Text.all._

/**
 * Generates a logo for the Scala debugger project.
 */
object FrontPageLogo {
  lazy val AHref = "/"
  lazy val ImgSrc = Images.inlineEnsimeLogoNoText
  lazy val ImgWidth = "110px"
  lazy val ImgHeight = "30px"

  def apply(): Modifier = {
    val q = "\""
    a(
      href := AHref,
      position := "relative",
      display := "inline-block",
      width := "123px",
      verticalAlign := "top",
      marginRight := "20px"
    )(
      img(
        src := ImgSrc,
        display := "block",
        width := ImgWidth,
        height := ImgHeight,
        position := "absolute",
        left := "0",
        right := "0",
        top := "0",
        bottom := "0",
        margin := "auto"
      )()
    )
  }
}
