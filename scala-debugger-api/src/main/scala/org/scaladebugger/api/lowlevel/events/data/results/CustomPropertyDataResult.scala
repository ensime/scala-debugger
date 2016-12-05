package org.scaladebugger.api.lowlevel.events.data.results

import org.scaladebugger.api.lowlevel.events.data.JDIEventDataResult

/**
 * Represents the result from retrieving a custom property.
 *
 * @param key The key of the custom property
 * @param value The value of the custom property
 */
case class CustomPropertyDataResult(
  key: AnyRef, value: AnyRef
) extends JDIEventDataResult
