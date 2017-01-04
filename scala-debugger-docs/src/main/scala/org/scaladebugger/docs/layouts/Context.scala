package org.scaladebugger.docs.layouts

import org.scaladebugger.docs.layouts.partials.common.MenuItem

/**
 * Represents the contextual information passed to all layouts.
 *
 * @param mainMenuItems The menu items for the main menu
 * @param sideMenuItems The menu items for the side menu
 */
case class Context(
  mainMenuItems: Seq[MenuItem] = Nil,
  sideMenuItems: Seq[MenuItem] = Nil
)
