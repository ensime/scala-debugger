package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates the menu logo for the Scala debugger project.
 */
object MainMenuLogo {
  def apply(): Modifier = {
    a(MainNavStyle.navLogo, href := "/")(
      EnsimeLogo(),
      span("Scala Debugger")
    )
  }
}
