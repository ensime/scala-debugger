package org.scaladebugger.docs.layouts.partials.common.vendor

import scalatags.Text.all._

/**
 * Represents a <script ... > containing highlight.js.
 */
object HighlightJS {
  def apply(): Modifier = {
    script(src := "/scripts/vendor/highlight/highlight.pack.js")
  }
}
