package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{InfoProducerProfile, MethodInfoProfile, ReferenceTypeInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureLocationInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockNewMethodProfile = mockFunction[Method, MethodInfoProfile]
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducerProfile]
  private val mockLocation = mock[Location]
  private val pureLocationInfoProfile = new PureLocationInfoProfile(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducerProfile,
    _location = mockLocation
  ) {
    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfoProfile = mockNewReferenceTypeProfile(referenceType)

    override protected def newMethodProfile(
      method: Method
    ): MethodInfoProfile = mockNewMethodProfile(method)
  }

  describe("PureLocationInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockLocation

        val actual = pureLocationInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#declaringType") {
      it("should return a wrapper profile for the declaring type of the location") {
        val expected = mock[ReferenceTypeInfoProfile]
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
        val expected = mock[MethodInfoProfile]
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
