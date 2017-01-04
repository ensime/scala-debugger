package org.scaladebugger.docs.layouts.partials.common

import java.net.URL
import java.util.Calendar

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._

/**
 * Generates front page footer.
 */
object Footer {
  def apply(authorName: String, authorUrl: URL, startYear: Int): Modifier = {
    tag("footer")(PageStyle.footerCls, PageStyle.sectionDark)(
      div(PageStyle.footerContent)(
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
