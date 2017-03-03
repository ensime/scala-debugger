package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._
import scalatags.stylesheet.Cls

/**
 * Generates a button.
 */
object Button {
  def apply(name: String, link: String, extraClasses: Cls*): Modifier =
    div(PageStyle.buttonCls, extraClasses)(a(width := "100%", height := "100%", href := link)(name))
}
