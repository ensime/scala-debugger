package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._

/**
 * Generates a button.
 */
object Button {
  def apply(name: String, link: String): Modifier =
    div(PageStyle.buttonCls)(a(href := link)(name))
}
