package org.scaladebugger.api.lowlevel.classes
import acyclic.file
import com.sun.jdi.{Field, Location, Method, ReferenceType}

import collection.JavaConverters._

/**
 * Represents the container for constants used in the class manager.
 */
object ClassManager {
  /** Used as the "file name" for classes with no 'file' that match arrays. */
  val DefaultArrayGroupName = "ARRAY"

  /** Used as the "file name" for classes with no 'file' that match nothing. */
  val DefaultUnknownGroupName = "UNKNOWN"
}

/**
 * Represents a manager of classes available on the virtual machine and their
 * associated files.
 */
trait ClassManager {
  /**
   * Retrieves the mapping of lines to locations available for a specific file.
   *
   * @param fileName The name of the file whose lines and underlying
   *                  locations to retrieve
   *
   * @return Some mapping of file lines to associated locations in underlying
   *         JVM classes if the file exists, otherwise None
   */
  def linesAndLocationsForFile(
    fileName: String
  ): Option[Map[Int, Seq[Location]]]

  /**
   * Determines whether or not there is a class with the provided
   * fully-qualified class name.
   *
   * @param className The fully-qualified class name
   *
   * @return True if a class exists, otherwise false
   */
  def hasClassWithName(className: String): Boolean =
    classesWithName(className).nonEmpty

  /**
   * Retrieves all class references associated with the provided
   * fully-qualified class name.
   *
   * @param className The fully-qualified class name
   *
   * @return The collection of reference types representing the class
   */
  def classesWithName(className: String): Seq[ReferenceType]

  /**
   * Determines whether or not there is a method with the provided name.
   *
   * @param className The fully-qualified class name of the class whose
   *                  methods to inspect
   * @param methodName The name of the method to check
   *
   * @return True if the method exists, otherwise false
   */
  def hasMethodWithName(className: String, methodName: String): Boolean =
    methodsWithName(className: String, methodName: String).nonEmpty

  /**
   * Determines whether or not there is a method with the provided name.
   *
   * @param className The fully-qualified class name of the class whose
   *                  methods to inspect
   * @param methodName The name of the method to check
   *
   * @return True if the method exists, otherwise false
   */
  def methodsWithName(className: String, methodName: String): Seq[Method] =
    classesWithName(className).flatMap(_.allMethods().asScala)
      .filter(_.name() == methodName)

  /**
   * Determines whether or not there is a field with the provided name.
   *
   * @param className The fully-qualified class name of the class whose
   *                  methods to inspect
   * @param fieldName The name of the field to check
   *
   * @return True if the method exists, otherwise false
   */
  def hasFieldWithName(className: String, fieldName: String): Boolean =
    fieldsWithName(className: String, fieldName: String).nonEmpty

  /**
   * Determines whether or not there is a field with the provided name.
   *
   * @param className The fully-qualified class name of the class whose
   *                  methods to inspect
   * @param fieldName The name of the field to check
   *
   * @return True if the method exists, otherwise false
   */
  def fieldsWithName(className: String, fieldName: String): Seq[Field] =
    classesWithName(className).flatMap(_.allFields().asScala)
      .filter(_.name() == fieldName)

  /**
   * Retrieves the list of underlying JVM classes for the specified file.
   *
   * @param fileName The name of the file whose underlying representations
   *                  to retrieve
   *
   * @return Some list of underlying class references if the file name can
   *         be found, otherwise None
   */
  def underlyingReferencesForFile(fileName: String): Option[Seq[ReferenceType]]

  /**
   * Refresh the list of classes contained by the underlying virtual machine.
   * Groups by source path, falling back to a standard "ARRAY" grouping for
   * references to array structures and "UNKNOWN" for references with no
   * source name or known name.
   */
  def refreshAllClasses(): Unit

  /**
   * Refresh a single class given the reference type.
   *
   * @param referenceType The reference type used for the refresh
   */
  def refreshClass(referenceType: ReferenceType): Unit

  /**
   * Retrieves the file name for the associated reference type.
   *
   * @param referenceType The reference type whose file name to retrieve
   *
   * @return The file name if it exists, otherwise ARRAY if the reference type
   *         is an array or UNKNOWN if it is not
   */
  def fileNameForReferenceType(referenceType: ReferenceType): String

  /**
   * Retrieves a list of available (cached) Scala file names.
   *
   * @return The collection of file names
   */
  def allScalaFileNames: Seq[String] =
    allFileNamesWithExtension("scala")

  /**
   * Retrieves a list of available (cached) Java file names.
   *
   * @return The collection of file names
   */
  def allJavaFileNames: Seq[String] =
    allFileNamesWithExtension("java")

  /**
   * Retrieves a list of available (cached) file names with the provided
   * extension.
   *
   * @param extension The extension of the file names (Scala/Java/etc)
   *
   * @return The collection of file names
   */
  def allFileNamesWithExtension(extension: String): Seq[String] =
    allFileNames.filter(_.endsWith(extension))

  /**
   * Retrieves a list of all available (cached) file names.
   *
   * @return The collection of file names
   */
  def allFileNames: Seq[String]

  /**
   * Retrieves a list of all available (cached) classes.
   *
   * @return The collection of reference types
   */
  def allClasses: Seq[ReferenceType]
}
