package org.scaladebugger.api.lowlevel.exceptions

/**
 * Represents an exception that occurred when attempting to create an exception
 * request and the desired exception class was not found on the remote JVM.
 *
 * @param className The name of the class of exception
 */
case class NoExceptionClassFound(className: String)
  extends Throwable(s"No exception for $className was found!")
