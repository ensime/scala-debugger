package org.scaladebugger.api.profiles.pure.info
import acyclic.file
import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, ReferenceTypeInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureMiscInfoProfileSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockClassManager = mock[ClassManager]

  private val mockRetrieveCommandLineArguments = mockFunction[Seq[String]]
  private val mockRetrieveMainClassName = mockFunction[String]
  private val mockMiscNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]

  private val pureMiscInfoProfile = new Object with PureMiscInfoProfile {
    override protected def retrieveCommandLineArguments(): Seq[String] =
      mockRetrieveCommandLineArguments()
    override protected def retrieveMainClassName(): String =
      mockRetrieveMainClassName()
    override protected def miscNewReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfoProfile = mockMiscNewReferenceTypeProfile(referenceType)
    override protected val classManager: ClassManager = mockClassManager
    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
    override protected val infoProducer: InfoProducerProfile = mockInfoProducerProfile
    override protected val _virtualMachine: VirtualMachine = mockVirtualMachine
  }

  describe("PureMiscInfoProfile") {
    describe("#availableLinesForFile") {
      it("should return the lines (sorted) that can have breakpoints") {
        val expected = Seq(1, 8, 999)

        // Setup the return from class manager to be reverse order
        val linesAndLocations = expected.reverseMap(i =>
          (i, Seq(stub[Location]))
        ).toMap
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(linesAndLocations))

        val actual = pureMiscInfoProfile.availableLinesForFile("").get

        actual should contain theSameElementsInOrderAs expected
      }

      it("should return None if the file does not exist") {
        val expected = None

        // Set the return from class manager to be "not found"
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(None)

        val actual = pureMiscInfoProfile.availableLinesForFile("")

        actual should be (expected)
      }
    }
    describe("#sourceNameToPaths") {
      it("should ignore any class with absent source name information") {
        val expected = Nil
        val sourceName = "file.scala"

        val referenceTypeProfiles = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        // All classes are examined for their sources
        (mockClassManager.allClasses _).expects()
          .returning(referenceTypes).once()

        // Class references are transformed into our profile structure
        referenceTypeProfiles.zip(referenceTypes).foreach { case (p, r) =>
          mockMiscNewReferenceTypeProfile.expects(r).returning(p).once()
        }

        // Accessing the source names can fail
        referenceTypeProfiles.foreach(p =>
          (p.trySourceNames _).expects()
            .returning(Failure(new AbsentInformationException)).once()
        )

        val actual = pureMiscInfoProfile.sourceNameToPaths(sourceName)

        actual should be (expected)
      }

      it("should ignore any class with absent source path information") {
        val expected = Nil
        val sourceName = "file.scala"

        val referenceTypeProfiles = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        // All classes are examined for their sources
        (mockClassManager.allClasses _).expects()
          .returning(referenceTypes).once()

        // Class references are transformed into our profile structure
        referenceTypeProfiles.zip(referenceTypes).foreach { case (p, r) =>
          mockMiscNewReferenceTypeProfile.expects(r).returning(p).once()
        }

        // Filtering by source name (return matching name)
        referenceTypeProfiles.foreach(p =>
          (p.trySourceNames _).expects()
            .returning(Success(Seq(sourceName))).once()
        )

        // Accessing the source paths can fail
        referenceTypeProfiles.foreach(p =>
          (p.trySourcePaths _).expects()
            .returning(Failure(new AbsentInformationException)).once()
        )

        val actual = pureMiscInfoProfile.sourceNameToPaths(sourceName)

        actual should be (expected)
      }

      it("should collect source paths for all classes with the same source name") {
        val expected = Seq("path/to/file.scala", "other/path/to/file.scala")
        val sourceName = "file.scala"

        val referenceTypeProfiles = Seq(mock[ReferenceTypeInfoProfile])
        val referenceTypes = Seq(mock[ReferenceType])

        // All classes are examined for their sources
        (mockClassManager.allClasses _).expects()
          .returning(referenceTypes).once()

        // Class references are transformed into our profile structure
        referenceTypeProfiles.zip(referenceTypes).foreach { case (p, r) =>
          mockMiscNewReferenceTypeProfile.expects(r).returning(p).once()
        }

        // Filtering by source name (return matching name)
        referenceTypeProfiles.foreach(p =>
          (p.trySourceNames _).expects()
            .returning(Success(Seq(sourceName))).once()
        )

        // Source paths returned by each reference type profile are included
        // in final results
        referenceTypeProfiles.foreach(p =>
          (p.trySourcePaths _).expects()
            .returning(Success(expected)).once()
        )

        val actual = pureMiscInfoProfile.sourceNameToPaths(sourceName)

        actual should contain theSameElementsAs (expected)
      }
    }

    describe("#mainClassName") {
      it("should invoke the JDI Helper method equivalent") {
        val expected = "some main class name"

        mockRetrieveMainClassName.expects().returning(expected).once()
        val actual = pureMiscInfoProfile.mainClassName

        actual should be (expected)
      }
    }

    describe("#commandLineArguments") {
      it("should invoke the JDI Helper method equivalent") {
        val expected = Seq("some", "arguments")

        mockRetrieveCommandLineArguments.expects().returning(expected).once()
        val actual = pureMiscInfoProfile.commandLineArguments

        actual should be (expected)
      }
    }
  }
}
