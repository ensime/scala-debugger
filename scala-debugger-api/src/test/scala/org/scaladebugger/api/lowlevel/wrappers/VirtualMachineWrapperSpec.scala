package org.scaladebugger.api.lowlevel.wrappers

import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.JDIMockHelpers

import scala.collection.JavaConverters._

class VirtualMachineWrapperSpec extends FunSpec with Matchers with MockFactory
  with JDIMockHelpers with ParallelTestExecution
{
  private val mockVirtualMachine = mock[VirtualMachine]
  private val virtualMachineWrapper =
    new VirtualMachineWrapper(mockVirtualMachine)

  describe("VirtualMachineWrapper") {
    describe("constructor") {
      it("should throw an exception if wrapping a null pointer") {
        intercept[IllegalArgumentException] {
          new VirtualMachineWrapper(null)
        }
      }
    }

    describe("#mirrorOf") {
      it("should convert a boolean to a mirror value") {
        val expected = mock[BooleanValue]

        val value = true
        (mockVirtualMachine.mirrorOf(_: Boolean)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert a byte to a mirror value") {
        val expected = mock[ByteValue]

        val value: Byte = 0
        (mockVirtualMachine.mirrorOf(_: Byte)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert a char to a mirror value") {
        val expected = mock[CharValue]

        val value = 'a'
        (mockVirtualMachine.mirrorOf(_: Char)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert an integer to a mirror value") {
        val expected = mock[IntegerValue]

        val value: Int = 33
        (mockVirtualMachine.mirrorOf(_: Int)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert a short to a mirror value") {
        val expected = mock[ShortValue]

        val value: Short = 33
        (mockVirtualMachine.mirrorOf(_: Short)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert a long to a mirror value") {
        val expected = mock[LongValue]

        val value: Long = 33L
        (mockVirtualMachine.mirrorOf(_: Long)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert a float to a mirror value") {
        val expected = mock[FloatValue]

        val value: Float = 0.0.toFloat
        (mockVirtualMachine.mirrorOf(_: Float)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert a double to a mirror value") {
        val expected = mock[DoubleValue]

        val value: Double = 0.0
        (mockVirtualMachine.mirrorOf(_: Double)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should convert a string to a mirror value") {
        val expected = mock[StringReference]

        val value: String = ""
        (mockVirtualMachine.mirrorOf(_: String)).expects(value)
          .returning(expected).once()

        val actual = virtualMachineWrapper.mirrorOf(value)

        actual should be (expected)
      }

      it("should throw an exception when receiving an unhandled value") {
        intercept[RuntimeException] {
          virtualMachineWrapper.mirrorOf(new AnyRef)
        }
      }
    }
  }
}
