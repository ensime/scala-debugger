package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.profiles.traits.info.{InfoProducer, ReferenceTypeInfo, ValueInfo}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class PureCreateInfoSpec extends test.ParallelMockFunSpec with JDIMockHelpers
{
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockCreateNewValueProfile = mockFunction[Value, ValueInfo]

  private val pureCreateInfoProfile = new Object with PureCreateInfoProfile {
    override protected def createNewValueProfile(value: Value): ValueInfo =
      mockCreateNewValueProfile(value)

    override protected val scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachine
    override protected val infoProducer: InfoProducer = mockInfoProducerProfile
    override protected val _virtualMachine: VirtualMachine = mockVirtualMachine
  }

  describe("PureCreateInfoProfile") {
    describe("#createRemotely(AnyVal)") {
      it("should create and wrap a mirrored boolean value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Boolean = true

        val mockValue = mock[BooleanValue]
        (mockVirtualMachine.mirrorOf(_: Boolean)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }

      it("should create and wrap a mirrored byte value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Byte = 33

        val mockValue = mock[ByteValue]
        (mockVirtualMachine.mirrorOf(_: Byte)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }

      it("should create and wrap a mirrored char value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Char = 33

        val mockValue = mock[CharValue]
        (mockVirtualMachine.mirrorOf(_: Char)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }

      it("should create and wrap a mirrored integer value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Int = 33

        val mockValue = mock[IntegerValue]
        (mockVirtualMachine.mirrorOf(_: Int)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }

      it("should create and wrap a mirrored short value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Short = 33

        val mockValue = mock[ShortValue]
        (mockVirtualMachine.mirrorOf(_: Short)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }

      it("should create and wrap a mirrored long value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Long = 33

        val mockValue = mock[LongValue]
        (mockVirtualMachine.mirrorOf(_: Long)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }

      it("should create and wrap a mirrored float value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Float = 33

        val mockValue = mock[FloatValue]
        (mockVirtualMachine.mirrorOf(_: Float)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }

      it("should create and wrap a mirrored double value in a value info profile") {
        val expected = mock[ValueInfo]
        val testValue: Double = 33

        val mockValue = mock[DoubleValue]
        (mockVirtualMachine.mirrorOf(_: Double)).expects(testValue)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testValue)

        actual should be (expected)
      }
    }

    describe("#createRemotely(String)") {
      it("should create and wrap a mirrored string value in a value info profile") {
        val expected = mock[ValueInfo]
        val testString = "some string"

        val mockValue = mock[StringReference]
        (mockVirtualMachine.mirrorOf(_: String)).expects(testString)
          .returning(mockValue).once()

        mockCreateNewValueProfile.expects(mockValue).returning(expected).once()

        val actual = pureCreateInfoProfile.createRemotely(testString)

        actual should be (expected)
      }
    }
  }
}
