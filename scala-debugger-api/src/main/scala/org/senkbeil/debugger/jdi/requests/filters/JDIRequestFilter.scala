package org.senkbeil.debugger.jdi.requests.filters

import org.senkbeil.debugger.jdi.requests.processors.JDIRequestProcessor

/**
 * Represents a filter for a JDI Request.
 */
trait JDIRequestFilter {
  /**
   * Creates a new JDI request processor based on this filter.
   *
   * @return The new JDI request processor instance
   */
  def toProcessor: JDIRequestProcessor
}
