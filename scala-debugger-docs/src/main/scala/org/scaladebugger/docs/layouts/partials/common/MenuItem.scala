package org.scaladebugger.docs.layouts.partials.common

/**
 * Represents a generic menu item.
 *
 * @param name The name of the menu item
 * @param link The link for the menu item (defaults to #!)
 * @param children The children of the menu item
 * @param selected Whether or not the menu item is selected
 */
case class MenuItem(
  name: String,
  link: String = "#!",
  children: Seq[MenuItem] = Nil,
  selected: Boolean = false
)
