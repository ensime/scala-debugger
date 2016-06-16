package org.scaladebugger.api.profiles.scala210.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ReferenceTypeInfoProfile, TypeInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class Scala210FieldInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockObjectReference = mock[ObjectReference]
  private val mockField = mock[Field]
  private val scala210FieldInfoProfile = new Scala210FieldInfoProfile(
    mockScalaVirtualMachine,
    Left(mockObjectReference),
    mockField
  )(mockVirtualMachine) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)
  }

  describe("Scala210FieldInfoProfile") {
    describe("#name") {
      it("should return the raw name if normal") {
        val expected = "someName"

        (mockField.name _).expects().returning(expected).once()

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
