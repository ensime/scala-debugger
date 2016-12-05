package org.scaladebugger.api.lowlevel.classes
import org.scaladebugger.api.lowlevel.utils.JDIHelperMethods
import org.scaladebugger.api.utils.Logging
import com.sun.jdi.{Location, Method, ReferenceType, VirtualMachine}

import scala.collection.JavaConverters._
import scala.util.Try

import collection.mutable
import ClassManager._

/**
 * Represents a manager of classes available on the virtual machine and their
 * associated files.
 *
 * @param _virtualMachine The virtual machine whose classes to manage
 * @param loadClasses Whether or not to load all classes from the virtual
 *                    machine on initialization of this manager
 */
class StandardClassManager(
  protected val _virtualMachine: VirtualMachine,
  loadClasses: Boolean = true
) extends ClassManager with JDIHelperMethods with Logging {
  /** Mapping of file names to associated classes. */
  private val fileToClasses = mutable.Map[String, Seq[ReferenceType]]()

  /**
   * Retrieves the mapping of lines to locations available for a specific file.
   *
   * @param fileName The name of the file whose lines and underlying
   *                  locations to retrieve
   *
   * @return Some mapping of file lines to associated locations in underlying
   *         JVM classes if the file exists, otherwise None
   */
  override def linesAndLocationsForFile(
    fileName: String
  ): Option[Map[Int, Seq[Location]]] = {
    def linesForReferenceType(referenceType: ReferenceType): Seq[Location] = {
      Try(referenceType.allLineLocations())
        .map(_.asScala).getOrElse(Nil)
        .filter(location => Try(location.lineNumber()).isSuccess)
    }

    // Combine all lines for underlying reference types together
    underlyingReferencesForFile(fileName).map { referenceTypes =>
      referenceTypes
        .map(linesForReferenceType)
        .reduce(_ ++ _)
        .groupBy(_.lineNumber())
    }
  }

  /**
   * Retrieves all class references associated with the provided
   * fully-qualified class name.
   *
   * @param className The fully-qualified class name
   * @return The collection of reference types representing the class
   */
  override def classesWithName(className: String): Seq[ReferenceType] =
    _virtualMachine.classesByName(className).asScala

  /**
   * Retrieves the list of underlying JVM classes for the specified file.
   *
   * @param fileName The name of the file whose underlying representations
   *                  to retrieve
   * @return Some list of underlying class references if the file name can
   *         be found, otherwise None
   */
  override def underlyingReferencesForFile(
    fileName: String
  ): Option[Seq[ReferenceType]] = fileToClasses.get(fileName)

  /**
   * Refresh the list of classes contained by the underlying virtual machine.
   * Groups by source path, falling back to a standard "ARRAY" grouping for
   * references to array structures and "UNKNOWN" for references with no
   * source name or known name.
   */
  override def refreshAllClasses(): Unit =
    _virtualMachine.allClasses().asScala.foreach(refreshClass)

  /**
   * Refresh a single class given the reference type.
   *
   * @param referenceType The reference type used for the refresh
   */
  override def refreshClass(referenceType: ReferenceType): Unit = {
    val fileName = fileNameForReferenceType(referenceType)

    logger.trace(s"Refreshing ${referenceType.name()} in $fileName!")

    // NOTE: Assuming that we do not get an existing reference type!
    val existingClasses = fileToClasses.getOrElse(fileName, Nil)
    fileToClasses.put(fileName, existingClasses :+ referenceType)
  }

  /**
   * Retrieves the file name for the associated reference type.
   *
   * @param referenceType The reference type whose file name to retrieve
   *
   * @return The file name if it exists, otherwise ARRAY if the reference type
   *         is an array or UNKNOWN if it is not
   */
  override def fileNameForReferenceType(referenceType: ReferenceType): String =
    singleSourcePath(referenceType).getOrElse(
      if (referenceType.name().endsWith("[]")) DefaultArrayGroupName
      else DefaultUnknownGroupName
    )

  /**
   * Retrieves a list of all available (cached) file names.
   *
   * @return The collection of file names
   */
  override def allFileNames: Seq[String] = fileToClasses.keys.toSeq

  /**
   * Retrieves a list of all available (cached) classes.
   *
   * @return The collection of reference types
   */
  override def allClasses: Seq[ReferenceType] =
    fileToClasses.values.toSeq.flatten

  // ==========================================================================
  // = CONSTRUCTOR
  // ==========================================================================

  // If marked to load classes during the constructor, do so now
  if (loadClasses) refreshAllClasses()
}
