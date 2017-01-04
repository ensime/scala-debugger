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
  def render(content: Seq[Modifier] = Nil): Modifier

  /**
   * Renders the layout with content and returns the string representation.
   *
   * @param content The content to fill in the layout
   * @return The string representation of the layout
   */
  def toString(content: Seq[Modifier]): String = render(content).toString

  /**
   * Renders the layout with no content and returns the string representation.
   *
   * @return The string representation of the layout
   */
  override def toString: String = toString(Nil)
}
