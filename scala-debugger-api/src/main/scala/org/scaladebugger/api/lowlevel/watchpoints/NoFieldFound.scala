package org.scaladebugger.api.lowlevel.watchpoints

/**
 * Represents an exception that occurred when attempting to create a watchpoint
 * request and the desired class or field was not found on the remote JVM.
 *
 * @param className The name of the class containing the field
 * @param fieldName The name of the field to watch
 */
case class NoFieldFound(className: String, fieldName: String)
  extends Throwable(s"No field for $className.$fieldName was found!")
