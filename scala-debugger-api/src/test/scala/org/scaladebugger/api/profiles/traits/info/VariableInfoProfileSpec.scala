package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.{TestValueInfoProfile, TestVariableInfoProfile}

import scala.util.{Failure, Success, Try}

class VariableInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("VariableInfoProfile") {
    describe("#trySetValue(AnyVal)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[AnyVal, ValueInfoProfile]

        val variableInfoProfile = new TestVariableInfoProfile {
          override def setValue(value: AnyVal): ValueInfoProfile =
            mockUnsafeMethod(value)
        }

        val a1: AnyVal = 333
        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        variableInfoProfile.trySetValue(a1).get should be (r)
      }
    }

    describe("#setValue(AnyVal)") {
      it("should convert the local value to a remote value and set it") {
        val mockCreateRemotely = mockFunction[AnyVal, ValueInfoProfile]
        val mockSetValueFromInfo = mockFunction[ValueInfoProfile, ValueInfoProfile]
        val variableInfoProfile = new TestVariableInfoProfile {
          override def createRemotely(value: AnyVal): ValueInfoProfile =
            mockCreateRemotely(value)

          override def setValueFromInfo(
            valueInfo: ValueInfoProfile
          ): ValueInfoProfile = mockSetValueFromInfo(valueInfo)
        }

        val a1: AnyVal = 33
        val r = mock[ValueInfoProfile]
        mockCreateRemotely.expects(a1).returning(r).once()
        mockSetValueFromInfo.expects(r).returning(r).once()
        variableInfoProfile.setValue(a1) should be (r)
      }    }

    describe("#trySetValue(String)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ValueInfoProfile]

        val variableInfoProfile = new TestVariableInfoProfile {
          override def setValue(value: String): ValueInfoProfile =
            mockUnsafeMethod(value)
        }

        val a1 = "some string"
        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        variableInfoProfile.trySetValue(a1).get should be (r)
      }
    }

    describe("#setValue(String)") {
      it("should convert the local value to a remote value and set it") {
        val mockCreateRemotely = mockFunction[String, ValueInfoProfile]
        val mockSetValueFromInfo = mockFunction[ValueInfoProfile, ValueInfoProfile]
        val variableInfoProfile = new TestVariableInfoProfile {
          override def createRemotely(value: String): ValueInfoProfile =
            mockCreateRemotely(value)

          override def setValueFromInfo(
            valueInfo: ValueInfoProfile
          ): ValueInfoProfile = mockSetValueFromInfo(valueInfo)
        }

        val a1 = "some string"
        val r = mock[ValueInfoProfile]
        mockCreateRemotely.expects(a1).returning(r).once()
        mockSetValueFromInfo.expects(r).returning(r).once()
        variableInfoProfile.setValue(a1) should be (r)
      }
    }

    describe("#trySetValueFromInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ValueInfoProfile, ValueInfoProfile]

        val variableInfoProfile = new TestVariableInfoProfile {
          override def setValueFromInfo(
            valueInfo: ValueInfoProfile
          ): ValueInfoProfile = mockUnsafeMethod(valueInfo)
        }

        val a1 = mock[ValueInfoProfile]
        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects(a1).returning(r).once()
        variableInfoProfile.trySetValueFromInfo(a1).get should be (r)
      }
    }

    describe("#toPrettyString") {
      it("should display the variable name and pretty-stringed value") {
        val expected = "someName = someValue"

        val mockValueInfoProfile = mock[ValueInfoProfile]
        (mockValueInfoProfile.toPrettyString _).expects()
          .returning("someValue").once()

        val variableInfoProfile = new TestVariableInfoProfile {
          override def name: String = "someName"
          override def tryToValue: Try[ValueInfoProfile] =
            Success(mockValueInfoProfile)
        }

        val actual = variableInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display ??? for the value if unavailable") {
        val expected = "someName = ???"

        val variableInfoProfile = new TestVariableInfoProfile {
          override def name: String = "someName"
          override def tryToValue: Try[ValueInfoProfile] =
            Failure(new Throwable)
        }

        val actual = variableInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#toValue") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ValueInfoProfile]

        val variableInfoProfile = new TestVariableInfoProfile {
          override def toValue: ValueInfoProfile = mockUnsafeMethod()
        }

        val r = mock[ValueInfoProfile]
        mockUnsafeMethod.expects().returning(r).once()
        variableInfoProfile.tryToValue.get should be (r)
      }
    }
  }
}
