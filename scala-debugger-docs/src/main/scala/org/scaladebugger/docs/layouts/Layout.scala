package org.scaladebugger.docs.layouts

import scalatags.Text.all._

/**
 * Represents the base interface that a layout must implement.
 */
trait Layout {
  /**
   * Renders the provided content as HTML using this layout.
   *
   * @param content The content to render as HTML using this layout
   * @return The rendered content
   */
  def apply(content: Seq[Modifier] = Nil): Modifier = render(content)

  /**
   * Renders the provided content as HTML using this layout.
   *
   * @param content The content to render as HTML using this layout
   * @return The rendered content
   */
  def render(content: Seq[Modifier] = Nil): Modifier
}
