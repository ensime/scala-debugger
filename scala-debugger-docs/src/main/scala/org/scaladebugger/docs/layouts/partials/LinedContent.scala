package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.FrontPageStyle

import scalatags.Text.all._

/**
 * Generates a lined content structure.
 */
object LinedContent {
  def apply(markerText: String, content: Modifier): Modifier = Raw(
    span(FrontPageStyle.marker, FrontPageStyle.linedContentLeft)(markerText),
    span(FrontPageStyle.linedContentRight)(content)
  )

  /** Takes raw content to place inside a lined content block. */
  def Raw(content: Modifier*): Modifier =
    div(FrontPageStyle.linedContent)(content: _*)
}
