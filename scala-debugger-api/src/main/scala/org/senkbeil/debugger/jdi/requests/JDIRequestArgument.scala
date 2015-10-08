package org.senkbeil.debugger.jdi.requests

import org.senkbeil.debugger.jdi.requests.processors.JDIRequestProcessor

/**
 * Represents an argument for a JDI Request.
 */
trait JDIRequestArgument {
  /**
   * Creates a new JDI request processor based on this argument.
   *
   * @return The new JDI request processor instance
   */
  def toProcessor: JDIRequestProcessor
}
