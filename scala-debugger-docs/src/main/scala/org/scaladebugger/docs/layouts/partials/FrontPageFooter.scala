package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates front page footer.
 */
object FrontPageFooter {
  private lazy val hyphenSymbol = "&#8212;"
  private lazy val copyrightSymbol = "&#169;"
  private lazy val copyrightStartYear = 2015
  private lazy val copyrightEndYear = 2016

  private lazy val author = "Chip Senkbeil"
  private lazy val authorSite = "https://chipsenkbeil.com"

  def apply(content: Modifier*): Modifier = {
    val copyright = s"$hyphenSymbol $copyrightSymbol $copyrightStartYear $copyrightEndYear"

    tag("footer")(FrontPageStyle.footerCls)(
      raw(s"""All code for this site is open source. $copyright """),
      a(href := authorSite)(author)
    )
  }
}
