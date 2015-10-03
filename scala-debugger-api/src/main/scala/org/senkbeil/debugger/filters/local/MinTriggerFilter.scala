package org.senkbeil.debugger.filters.local

/**
 * Represents a local filter that will result in ignoring any incoming event
 * until N successful events have been reported.
 *
 * @example MinTriggerFilter(count = 3) will ingore event 1, 2, and 3; allowing
 *          all subsequent events
 *
 * @param count The total number of events to ignore before allowing all
 *              subsequent events
 */
case class MinTriggerFilter(count: Int)
