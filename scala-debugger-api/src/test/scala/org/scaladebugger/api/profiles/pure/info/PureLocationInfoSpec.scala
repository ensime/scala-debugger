package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, LocationInfo, MethodInfo, ReferenceTypeInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureLocationInfoSpec extends ParallelMockFunSpec
{
  private val mockNewMethodProfile = mockFunction[Method, MethodInfo]
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockLocation = mock[Location]
  private val pureLocationInfoProfile = new PureLocationInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducerProfile,
    _location = mockLocation
  ) {
    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfo = mockNewReferenceTypeProfile(referenceType)

    override protected def newMethodProfile(
      method: Method
    ): MethodInfo = mockNewMethodProfile(method)
  }

  describe("PureLocationInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[LocationInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newLocationInfo _)
          .expects(mockScalaVirtualMachine, mockLocation)
          .returning(expected).once()

        val actual = pureLocationInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureLocationInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockLocation

        val actual = pureLocationInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#declaringType") {
      it("should return a wrapper profile for the declaring type of the location") {
        val expected = mock[ReferenceTypeInfo]
        val referenceType = mock[ReferenceType]

        (mockLocation.declaringType _).expects()
          .returning(referenceType).once()

        mockNewReferenceTypeProfile.expects(referenceType)
          .returning(expected).once()

        val actual = pureLocationInfoProfile.declaringType

        actual should be (expected)
      }
    }

    describe("#method") {
      it("should return a wrapper profile for the method of the location") {
        val expected = mock[MethodInfo]
        val referenceType = mock[Method]

        (mockLocation.method _).expects()
          .returning(referenceType).once()

        mockNewMethodProfile.expects(referenceType)
          .returning(expected).once()

        val actual = pureLocationInfoProfile.method

        actual should be (expected)
      }
    }

    describe("#codeIndex") {
      it("should return the code index of the underlying location") {
        val expected = 999

        (mockLocation.codeIndex _).expects().returning(expected).once()

        val actual = pureLocationInfoProfile.codeIndex

        actual should be (expected)
      }
    }

    describe("#lineNumber") {
      it("should return the line number of the underlying location") {
        val expected = 999

        (mockLocation.lineNumber: Function0[Int]).expects()
          .returning(expected).once()

        val actual = pureLocationInfoProfile.lineNumber

        actual should be (expected)
      }
    }

    describe("#sourceName") {
      it("should return the source name of the underlying location") {
        val expected = "file.scala"

        (mockLocation.sourceName: Function0[String]).expects()
          .returning(expected).once()

        val actual = pureLocationInfoProfile.sourceName

        actual should be (expected)
      }
    }

    describe("#sourcePath") {
      it("should return the source path of the underlying location") {
        val expected = "path/to/file.scala"

        (mockLocation.sourcePath: Function0[String]).expects()
          .returning(expected).once()

        val actual = pureLocationInfoProfile.sourcePath

        actual should be (expected)
      }
    }
  }
}
