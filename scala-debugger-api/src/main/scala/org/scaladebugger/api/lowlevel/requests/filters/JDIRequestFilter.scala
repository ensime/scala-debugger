package org.scaladebugger.api.lowlevel.requests.filters

import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

/**
 * Represents a filter for a JDI Request.
 */
trait JDIRequestFilter extends JDIRequestArgument {
  /**
   * Creates a new JDI request processor based on this filter.
   *
   * @return The new JDI request filter processor instance
   */
  def toProcessor: JDIRequestFilterProcessor
}
