package org.scaladebugger.api.lowlevel.methods

/**
 * Represents an exception that occurred when attempting to create a method
 * entry/exit request and the desired class or method was not found on the
 * remote JVM.
 *
 * @param className The name of the class where the method entry/exit was
 *                  attempted
 * @param methodName The name of the method where the method entry/exit was
 *                   attempted
 */
case class NoClassMethodFound(className: String, methodName: String)
  extends Throwable(s"No method for $methodName of class $className was found!")
