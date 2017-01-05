package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.{PageStyle, TopbarNavStyle}

import scalatags.Text.all._

/**
 * Generates the header.
 */
object Header {
  def apply(content: Modifier*): Modifier = {
    tag("header")(TopbarNavStyle.headerCls)(content)
  }
}
