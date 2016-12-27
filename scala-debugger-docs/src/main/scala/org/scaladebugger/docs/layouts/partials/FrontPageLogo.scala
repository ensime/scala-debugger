package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.extras.Images
import org.scaladebugger.docs.styles.Colors

import scalatags.Text.all._

/**
 * Generates a logo for the Scala debugger project.
 */
object FrontPageLogo {
  lazy val AHref = "/"
  lazy val ImgSrc = Images.inlineEnsimeLogoNoText

  def apply(): Modifier = {
    val q = "\""
    a(
      display := flex.cssName,
      alignItems := "center",
      justifyContent := "space-around",

      href := AHref,
      position := "relative",
      width := "250px",
      verticalAlign := "top",
      marginRight := "20px",

      color := Colors.EnsimeBlack,
      textDecoration := "none"
    )(
      img(
        src := ImgSrc,
        display := "block",
        height := "2em"
      )(),
      span(whiteSpace := "nowrap", height := "1em")("Scala Debugger")
    )
  }
}
