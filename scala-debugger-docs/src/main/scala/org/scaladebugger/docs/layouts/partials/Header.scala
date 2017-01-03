package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.{FrontPageStyle, MainNavStyle}

import scalatags.Text.all._

/**
 * Generates the header.
 */
object Header {
  def apply(content: Modifier*): Modifier = {
    tag("header")(MainNavStyle.headerCls, FrontPageStyle.sectionDark)(content)
  }
}
