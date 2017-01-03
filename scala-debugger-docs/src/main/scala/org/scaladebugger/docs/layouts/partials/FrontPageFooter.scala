package org.scaladebugger.docs.layouts.partials

import java.net.URL
import java.util.Calendar

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates front page footer.
 */
object FrontPageFooter {
  def apply(authorName: String, authorUrl: URL, startYear: Int): Modifier = {
    tag("footer")(FrontPageStyle.footerCls, FrontPageStyle.sectionDark)(
      div(FrontPageStyle.footerContent)(
        span(
          raw("Site contents "),
          i(`class` := "fa fa-copyright", attr("aria-hidden") := "true"),
          raw(" "),
          a(href := authorUrl.toString)(authorName),
          raw(s", $startYear-${Calendar.getInstance().get(Calendar.YEAR)}")
        )
      )
    )
  }
}
