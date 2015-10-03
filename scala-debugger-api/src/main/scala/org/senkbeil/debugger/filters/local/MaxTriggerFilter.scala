package org.senkbeil.debugger.filters.local

/**
 * Represents a local filter that will result in ignoring any incoming event
 * after N successful events have been reported.
 *
 * @example MaxTriggerFilter(count = 3) will allow event 1, 2, and 3; ignoring
 *          all subsequent events
 *
 * @param count The total number of events to allow before ignoring all
 *              subsequent events
 */
case class MaxTriggerFilter(count: Int)
