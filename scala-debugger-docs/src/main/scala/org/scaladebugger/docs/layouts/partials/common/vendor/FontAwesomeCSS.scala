package org.scaladebugger.docs.layouts.partials.common.vendor

import scalatags.Text.all._

/**
 * Represents a <link ... > containing font awesome stylesheet.
 */
object FontAwesomeCSS {
  def apply(): Modifier = {
    link(
      rel := "stylesheet",
      href := "/styles/vendor/font-awesome/css/font-awesome.min.css"
    )
  }
}
