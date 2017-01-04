package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.PageStyle

import scalatags.Text.all._

/**
 * Generates a lined content structure.
 */
object LinedContent {
  def apply(markerText: String, content: Modifier): Modifier = Raw(
    span(PageStyle.marker, PageStyle.linedContentLeft)(markerText),
    span(PageStyle.linedContentRight)(content)
  )

  /** Takes raw content to place inside a lined content block. */
  def Raw(content: Modifier*): Modifier =
    div(PageStyle.linedContent)(content: _*)
}
