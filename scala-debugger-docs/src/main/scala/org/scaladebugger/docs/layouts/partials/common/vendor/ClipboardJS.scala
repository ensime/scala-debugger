package org.scaladebugger.docs.layouts.partials.common.vendor

import scalatags.Text.all._

/**
 * Represents a <script ... > containing clipboard.js.
 */
object ClipboardJS {
  def apply(): Modifier = {
    script(src := "/scripts/vendor/clipboard/clipboard.min.js")
  }
}
