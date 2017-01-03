package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates a button.
 */
object Button {
  def apply(name: String, link: String): Modifier =
    div(FrontPageStyle.buttonCls)(a(href := link)(name))
}
