package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class Scala210FieldInfoSpec extends ParallelMockFunSpec
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockObjectReference = mock[ObjectReference]
  private val mockField = mock[Field]
  private val scala210FieldInfoProfile = new Scala210FieldInfo(
    mockScalaVirtualMachine,
    mockInfoProducerProfile,
    Left(mockObjectReference),
    mockField
  )(mockVirtualMachine) {
    override protected def newTypeProfile(_type: Type): TypeInfo =
      mockNewTypeProfile(_type)
  }

  describe("Scala210FieldInfo") {
    describe("#isJavaInfo") {
      it("should return false") {
        val expected = false

        val actual = scala210FieldInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the raw name if normal") {
        val expected = "someName"

        (mockField.name _).expects().returning(expected).once()

        val actual = scala210FieldInfoProfile.name

        actual should be (expected)
      }

      it("should return the name from the format name$number") {
        val expected = "someName"
        val rawName = expected + "$1"

        (mockField.name _).expects().returning(rawName).once()

        val actual = scala210FieldInfoProfile.name

        actual should be (expected)
      }

      it("should return the name from the format package$class$$name") {
        val expected = "someName"
        val rawName = "package$class$$" + expected

        (mockField.name _).expects().returning(rawName).once()

        val actual = scala210FieldInfoProfile.name

        actual should be (expected)
      }

      it("should return the name from the format package.class.name") {
        val expected = "someName"
        val rawName = "package.class." + expected

        (mockField.name _).expects().returning(rawName).once()

        val actual = scala210FieldInfoProfile.name

        actual should be (expected)
      }

      it("should return the raw name if no proper match is found") {
        val expected = "package$class$$"

        (mockField.name _).expects().returning(expected).once()

        val actual = scala210FieldInfoProfile.name

        actual should be (expected)
      }

      it("should return an empty string when the field name is empty") {
        val expected = ""
        val rawName = ""

        (mockField.name _).expects().returning(rawName).once()

        val actual = scala210FieldInfoProfile.name

        actual should be (expected)
      }
    }
  }
}
