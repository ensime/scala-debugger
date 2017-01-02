package org.scaladebugger.docs.layouts.partials

import org.scaladebugger.docs.styles.MainNavStyle

import scalatags.Text.all._

/**
 * Generates a logo for the Scala debugger project.
 */
object FrontPageLogo {
  def apply(): Modifier = {
    div(MainNavStyle.navLogo)(
      EnsimeLogo(),
      span("Scala Debugger")
    )
  }
}
