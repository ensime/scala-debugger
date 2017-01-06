package org.scaladebugger.docs.layouts.partials.common

/**
 * Represents a generic menu item.
 *
 * @param name The name of the menu item
 * @param link The link for the menu item, or None if the menu item does not
 *             link to any content
 * @param children The children of the menu item
 * @param selected Whether or not the menu item is selected
 */
case class MenuItem(
  name: String,
  link: Option[String] = None,
  children: Seq[MenuItem] = Nil,
  selected: Boolean = false
)
