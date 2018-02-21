package org.scaladebugger.api.profiles.traits.info

import acyclic.file
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

/**
 * Represents the interface that needs to be implemented to provide
 * miscellaneous info functionality for a specific debug profile.
 */
trait MiscInfo {
  /**
   * Retrieves the Scala virtual machine associated with this profile instance.
   *
   * @return The Scala virtual machine instance
   */
  def toScalaVirtualMachine: ScalaVirtualMachine

  /**
   * Retrieves the list of available lines for a specific file.
   *
   * @param fileName The name of the file whose lines to retrieve
   * @return Some list of breakpointable lines if the file exists,
   *         otherwise None
   */
  def availableLinesForFile(fileName: String): Option[Seq[Int]]

  /**
   * Retrieves all source paths for the given source name.
   *
   * @example nameToPaths("file.scala") yields
   *          Seq("path/to/file.scala", "other/path/to/file.scala")
   * @param sourceName The source (file) name whose associated paths to find
   * @return The collection of source paths
   */
  def sourceNameToPaths(sourceName: String): Seq[String]

  /**
   * Represents the name of the class used as the entrypoint for this vm.
   *
   * @return The main class name as a string
   */
  def mainClassName: String

  /**
   * Represents the command line arguments used to start this VM.
   *
   * @return The command line arguments as a collection of strings
   */
  def commandLineArguments: Seq[String]
}
