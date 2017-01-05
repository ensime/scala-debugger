package org.scaladebugger.docs.layouts.partials.common

import org.scaladebugger.docs.styles.TopbarNavStyle

import scalatags.Text.all._

/**
 * Generates the menu logo for the Scala debugger project.
 */
object MainMenuLogo {
  def apply(): Modifier = {
    a(TopbarNavStyle.navLogo, href := "/")(
      EnsimeLogo(),
      span("Scala Debugger")
    )
  }
}
