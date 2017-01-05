package org.scaladebugger.docs.styles

import scalatags.Text.all._
import scalatags.stylesheet._

/**
 * Contains stylesheet implicits.
 */
object Implicits {
  implicit class StyleSheetWrapper(private val styleSheet: StyleSheet) {
    /**
     * Transforms the stylesheet into a <style> tag.
     *
     * @return The style tag representing this stylesheet
     */
    def toStyleTag: Modifier = {
      // TODO: Check back in with https://github.com/lihaoyi/scalatags/issues/147
      throw new RuntimeException(
        "TODO: styleSheet.styleSheetText is unreliable and is not providing text!"
      )
      tag("style")(raw(styleSheet.styleSheetText))
    }
  }

  implicit class StringWrapper(private val string: String) {
    /**
     * Transforms the string into into a <style> tag.
     *
     * @return The style tag containing the raw text
     */
    def toStyleTag: Modifier = tag("style")(raw(string))
  }
}
