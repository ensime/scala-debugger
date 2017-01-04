package org.scaladebugger.docs.layouts.partials.common.vendor

import scalatags.Text.all._

/**
 * Represents a <link ... > containing font awesome stylesheet.
 */
object FontAwesome {
  def apply(): Modifier = {
    link(
      rel := "stylesheet",
      href := "https://opensource.keycdn.com/fontawesome/4.7.0/font-awesome.min.css",
      attr("integrity") := "sha384-dNpIIXE8U05kAbPhy3G1cz+yZmTzA6CY8Vg/u2L9xRnHjJiAK76m2BIEaSEV+/aU",
      attr("crossorigin") := "anonymous"
    )
  }
}
