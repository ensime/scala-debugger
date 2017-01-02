package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.{FrontPageStyle, MainNavStyle}

import scalatags.Text.all._

/**
 * Generates front page header.
 */
object FrontPageHeader {
  def apply(content: Modifier*): Modifier = {
    tag("header")(MainNavStyle.headerCls, FrontPageStyle.sectionDark)(content)
  }
}
