package org.scaladebugger.api.lowlevel.events.filters

import org.scaladebugger.api.lowlevel.events.filters.processors.CustomPropertyFilterProcessor

/**
 * Represents a local filter that will result in ignoring any incoming event if
 * it does not have a matching unique id.
 *
 * @example UniqueIdPropertyFilter("some id") will only allow events that have
 *          a request with a unique id set to "some id"
 *
 * @param id The unique id to check when filtering
 */
case class UniqueIdPropertyFilter(
  id: String
) extends JDIEventFilter with CustomPropertyFilterLike {
  /** Set to _id. */
  val key: AnyRef = "_id"

  /** Contains the unique id. */
  val value: AnyRef = id

  /**
   * Creates a new JDI event processor based on this filter.
   *
   * @return The new JDI event filter processor instance
   */
  override def toProcessor: JDIEventFilterProcessor =
    new CustomPropertyFilterProcessor(this)
}
