package org.scaladebugger.api.profiles.pure.info
//import acyclic.file

import com.sun.jdi.{ReferenceType, Value}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.utils.JDIHelperMethods
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, MiscInfoProfile, ReferenceTypeInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

import scala.util.Try

/**
 * Represents a pure profile for miscellaneous info that adds no extra logic
 * on top of the standard JDI.
 */
trait PureMiscInfoProfile extends MiscInfoProfile with JDIHelperMethods {
  protected val scalaVirtualMachine: ScalaVirtualMachine
  protected val infoProducer: InfoProducerProfile
  protected val classManager: ClassManager

  /**
   * Retrieves the list of available lines for a specific file.
   *
   * @param fileName The name of the file whose lines to retrieve
   * @return Some list of breakpointable lines if the file exists,
   *         otherwise None
   */
  override def availableLinesForFile(fileName: String): Option[Seq[Int]] =
    classManager.linesAndLocationsForFile(fileName).map(_.keys.toSeq.sorted)

  /**
   * Retrieves all source paths for the given source name.
   *
   * @example nameToPaths("file.scala") yields
   *          Seq("path/to/file.scala", "other/path/to/file.scala")
   * @param sourceName The source (file) name whose associated paths to find
   * @return The collection of source paths
   */
  override def sourceNameToPaths(sourceName: String): Seq[String] =
    classManager.allClasses
      .map(miscNewReferenceTypeProfile)
      .filter(r => r.trySourceNames.getOrElse(Nil).contains(sourceName))
      .flatMap(r => r.trySourcePaths.getOrElse(Nil))
      .distinct

  /**
   * Represents the command line arguments used to start this VM.
   *
   * @return The command line arguments as a collection of strings
   */
  override lazy val commandLineArguments: Seq[String] =
    retrieveCommandLineArguments()

  /**
   * Represents the name of the class used as the entrypoint for this vm.
   *
   * @return The main class name as a string
   */
  override lazy val mainClassName: String = retrieveMainClassName()

  protected def miscNewReferenceTypeProfile(
    referenceType: ReferenceType
  ): ReferenceTypeInfoProfile = infoProducer.newReferenceTypeInfoProfile(
    scalaVirtualMachine,
    referenceType
  )
}
