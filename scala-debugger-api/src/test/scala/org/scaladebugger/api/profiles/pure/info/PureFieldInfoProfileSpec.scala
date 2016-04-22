package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info.{TypeInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestMiscInfoProfileTrait

class PureFieldInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val mockNewTypeProfile = mockFunction[Type, TypeInfoProfile]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockObjectReference = mock[ObjectReference]
  private val mockField = mock[Field]
  private val pureFieldInfoProfile = new PureFieldInfoProfile(
    mockScalaVirtualMachine,
    Left(mockObjectReference),
    mockField
  )(mockVirtualMachine) {
    override protected def newTypeProfile(_type: Type): TypeInfoProfile =
      mockNewTypeProfile(_type)
  }

  describe("PureFieldInfoProfile") {
    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockField

        val actual = pureFieldInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the field's name") {
        val expected = "someName"

        (mockField.name _).expects().returning(expected).once()

        val actual = pureFieldInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#typeName") {
      it("should return the field's type name") {
        val expected = "some.type.name"

        (mockField.typeName _).expects().returning(expected).once()

        val actual = pureFieldInfoProfile.typeName

        actual should be (expected)
      }
    }

    describe("#typeInfo") {
      it("should should return a new type info profile wrapping the type") {
        val expected = mock[TypeInfoProfile]

        val mockType = mock[Type]
        (mockField.`type` _).expects().returning(mockType).once()

        mockNewTypeProfile.expects(mockType)
          .returning(expected).once()

        val actual = pureFieldInfoProfile.typeInfo

        actual should be (expected)
      }
    }

    describe("#isField") {
      it("should return true") {
        val expected = true

        val actual = pureFieldInfoProfile.isField

        actual should be (expected)
      }
    }

    describe("#isArgument") {
      it("should return false") {
        val expected = false

        val actual = pureFieldInfoProfile.isArgument

        actual should be (expected)
      }
    }

    describe("#isLocal") {
      it("should return false") {
        val expected = false

        val actual = pureFieldInfoProfile.isLocal

        actual should be (expected)
      }
    }

    describe("#setValueFromInfo") {
      it("should throw an exception if no object reference or class type available") {
        val pureFieldInfoProfile = new PureFieldInfoProfile(
          mockScalaVirtualMachine,
          Right(mock[ReferenceType]),
          mockField
        )(mockVirtualMachine)

        // Retrieval of JDI value still happens first
        val mockValueInfoProfile = mock[ValueInfoProfile]
        (mockValueInfoProfile.toJdiInstance _).expects()
          .returning(mock[Value]).once()

        intercept[Exception] {
          pureFieldInfoProfile.setValueFromInfo(mockValueInfoProfile)
        }
      }

      it("should be able to set instance fields") {
        val expected = mock[ValueInfoProfile]

        val mockStringReference = mock[StringReference]

        (expected.toJdiInstance _).expects()
          .returning(mockStringReference).once()

        // Ensure setting the value on the object is verified
        (mockObjectReference.setValue _)
          .expects(mockField, mockStringReference)
          .once()

        pureFieldInfoProfile.setValueFromInfo(expected) should be (expected)
      }

      it("should be able to set static fields") {
        val expected = mock[ValueInfoProfile]

        val mockClassType = mock[ClassType]
        val pureFieldInfoProfile = new PureFieldInfoProfile(
          mockScalaVirtualMachine,
          Right(mockClassType),
          mockField
        )(mockVirtualMachine)

        val mockStringReference = mock[StringReference]

        (expected.toJdiInstance _).expects()
          .returning(mockStringReference).once()

        // Ensure setting the value on the object is verified
        (mockClassType.setValue _)
          .expects(mockField, mockStringReference)
          .once()

        pureFieldInfoProfile.setValueFromInfo(expected) should be (expected)
      }
    }

    describe("#toValue") {
      it("should return a wrapper around the value of a class' static field") {
        val expected = mock[ValueInfoProfile]
        val mockValue = mock[Value]

        // Retrieving the value of the field returns our mock
        val mockClassType = mock[ClassType]
        (mockClassType.getValue _).expects(mockField)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]
        val pureFieldInfoProfile = new PureFieldInfoProfile(
          mockScalaVirtualMachine,
          Right(mockClassType),
          mockField
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureFieldInfoProfile.toValue should be (expected)
      }

      it("should return a wrapper around the value of an object's field instance") {
        val expected = mock[ValueInfoProfile]
        val mockValue = mock[Value]

        // Retrieving the value of the field returns our mock
        (mockObjectReference.getValue _).expects(mockField)
          .returning(mockValue).once()

        val mockNewValueProfile = mockFunction[Value, ValueInfoProfile]
        val pureFieldInfoProfile = new PureFieldInfoProfile(
          mockScalaVirtualMachine,
          Left(mockObjectReference),
          mockField
        )(mockVirtualMachine) {
          override protected def newValueProfile(value: Value): ValueInfoProfile =
            mockNewValueProfile(value)
        }

        mockNewValueProfile.expects(mockValue).returning(expected).once()
        pureFieldInfoProfile.toValue should be (expected)
      }
    }
  }
}
