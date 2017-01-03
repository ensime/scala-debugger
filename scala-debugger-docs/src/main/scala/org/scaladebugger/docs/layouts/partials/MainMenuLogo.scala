package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates the menu logo for the Scala debugger project.
 */
object MainMenuLogo {
  def apply(): Modifier = {
    div(MainNavStyle.navLogo)(
      EnsimeLogo(),
      span("Scala Debugger")
    )
  }
}
