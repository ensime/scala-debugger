package org.scaladebugger.docs.layouts.partials.common.vendor

import scalatags.Text.all._

/**
 * Represents a <script ... > containing highlight.js init code.
 */
object HighlightJSInit {
  def apply(): Modifier = {
    script("hljs.initHighlightingOnLoad();")
  }
}
