package org.scaladebugger.api.lowlevel.requests

import org.scaladebugger.api.lowlevel.JDIArgument

/**
 * Represents an argument for a JDI Request.
 */
trait JDIRequestArgument extends JDIArgument with Serializable {
  /**
   * Creates a new JDI request processor based on this argument.
   *
   * @return The new JDI request processor instance
   */
  def toProcessor: JDIRequestProcessor
}
