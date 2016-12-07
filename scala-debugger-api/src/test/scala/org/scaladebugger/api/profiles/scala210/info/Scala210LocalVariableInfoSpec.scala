package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine

class Scala210LocalVariableInfoSpec extends test.ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockFrameInfoProfile = mock[FrameInfo]
  private val mockLocalVariable = mock[LocalVariable]
  private val testOffsetIndex = 999
  private val scala210LocalVariableInfoProfile = new Scala210LocalVariableInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    mockFrameInfoProfile,
    mockLocalVariable,
    testOffsetIndex
  )(mockVirtualMachine) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("Scala210LocalVariableInfo") {
    describe("#isJavaInfo") {
      it("should return false") {
        val expected = false

        val actual = scala210LocalVariableInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the raw name if normal") {
        val expected = "someName"

        (mockLocalVariable.name _).expects().returning(expected).once()

        val actual = scala210LocalVariableInfoProfile.name

        actual should be (expected)
      }

      it("should return the name from the format name$number") {
        val expected = "someName"
        val rawName = expected + "$1"

        (mockLocalVariable.name _).expects().returning(rawName).once()

        val actual = scala210LocalVariableInfoProfile.name

        actual should be (expected)
      }

      it("should return the name from the format package$class$$name") {
        val expected = "someName"
        val rawName = "package$class$$" + expected

        (mockLocalVariable.name _).expects().returning(rawName).once()

        val actual = scala210LocalVariableInfoProfile.name

        actual should be (expected)
      }

      it("should return the name from the format package.class.name") {
        val expected = "someName"
        val rawName = "package.class." + expected

        (mockLocalVariable.name _).expects().returning(rawName).once()

        val actual = scala210LocalVariableInfoProfile.name

        actual should be (expected)
      }

      it("should return the raw name if no proper match is found") {
        val expected = "package$class$$"

        (mockLocalVariable.name _).expects().returning(expected).once()

        val actual = scala210LocalVariableInfoProfile.name

        actual should be (expected)
      }

      it("should return an empty string when the local variable name is empty") {
        val expected = ""
        val rawName = ""

        (mockLocalVariable.name _).expects().returning(rawName).once()

        val actual = scala210LocalVariableInfoProfile.name

        actual should be (expected)
      }
    }
  }
}
