package org.scaladebugger.docs.layouts.partials.common.vendor

import scalatags.Text.all._

/**
 * Represents a <script ... > containing clipboard.js.
 */
object ClipboardJS {
  def apply(): Modifier = {
    script(
      src := "https://cdnjs.cloudflare.com/ajax/libs/clipboard.js/1.5.16/clipboard.min.js"
    )
  }
}
