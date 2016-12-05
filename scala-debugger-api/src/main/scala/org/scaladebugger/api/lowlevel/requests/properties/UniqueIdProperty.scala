package org.scaladebugger.api.lowlevel.requests.properties

import org.scaladebugger.api.lowlevel.requests.properties.processors.CustomPropertyProcessor

/**
 * Represents an argument used set a unique id on the request.
 *
 * @param id The unique id to set for the request
 */
case class UniqueIdProperty(
  id: String
) extends JDIRequestProperty with CustomPropertyLike {
  /** Set to _id for lookup usage. */
  val key: AnyRef = "_id"

  /** Contains the unique id. */
  val value: AnyRef = id

  /**
   * Creates a new JDI request processor based on this property.
   *
   * @return The new JDI request processor instance
   */
  override def toProcessor: JDIRequestPropertyProcessor =
    new CustomPropertyProcessor(this)
}
