package org.senkbeil.debugger.filters.jdi

/**
 * Represents a filter used to restrict events until the specific event has
 * been reached "count" times. In other words, the first count-1 occurrences of
 * the event will not be triggered.
 *
 * Furthermore, the event will only be triggered once, meaning that setting a
 * count filter to 1 will cause the event to only fire once (on the first time
 * the event is reached).
 *
 * @param count The event will not be triggered the first "count - 1" times
 */
case class CountFilter(count: Int)
