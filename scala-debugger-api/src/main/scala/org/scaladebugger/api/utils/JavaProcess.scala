package org.scaladebugger.api.utils

import scala.util.Try

/**
 * Represents a Java process.
 *
 * @param pid The pid of the process
 * @param className The fully-qualified class name used as the entrypoint to the
 *                  Java process
 * @param jvmOptions The collection of JVM options provided to the Java process
 */
case class JavaProcess(
  pid: Long,
  className: String,
  jvmOptions: JVMOptions
)

object JavaProcess {
  /**
   * Parses a line from the JPS program when run with `jps -vl`.
   *
   * @example 1234 some.full.class -Doption=value --some-setting -option
   * @param line The line from the JPS program to parse
   * @param jvmOptionsFunc Optional function to use when parsing JVM options
   * @return The resulting information about the Java process
   */
  def fromJpsString(
    line: String,
    jvmOptionsFunc: (String) => JVMOptions = JVMOptions.fromOptionString
  ): Option[JavaProcess] = {
    require(line != null, "Line cannot be null!")

    val tokens = line.trim.split(" ")

    if (tokens.length < 2) return None

    val potentialPid = Try(tokens.head.toLong)
    if (potentialPid.isFailure) return None

    val pid = potentialPid.get
    val className = tokens(1)
    val jvmOptionsString = tokens.takeRight(tokens.length - 2).mkString(" ")
    val jvmOptions = jvmOptionsFunc(jvmOptionsString.trim)

    Some(JavaProcess(
      pid = pid,
      className = className,
      jvmOptions = jvmOptions
    ))
  }
}
