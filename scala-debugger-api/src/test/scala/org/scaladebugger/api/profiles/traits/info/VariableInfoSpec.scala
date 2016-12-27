package org.scaladebugger.api.profiles.traits.info

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.{TestValueInfo, TestVariableInfo}

import scala.util.{Failure, Success, Try}

class VariableInfoSpec extends ParallelMockFunSpec
{
  describe("VariableInfo") {
    describe("#hasOffsetIndex") {
      it("should return true if the offset index is greater than -1") {
        val expected = true

        val variableInfoProfile = new TestVariableInfo {
          override def offsetIndex: Int = 0
        }

        val actual = variableInfoProfile.hasOffsetIndex

        actual should be (expected)
      }

      it("should return false if the offset index is negative") {
        val expected = false

        val variableInfoProfile = new TestVariableInfo {
          override def offsetIndex: Int = -1
        }

        val actual = variableInfoProfile.hasOffsetIndex

        actual should be (expected)
      }
    }

    describe("#trySetValue(AnyVal)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[AnyVal, ValueInfo]

        val variableInfoProfile = new TestVariableInfo {
          override def setValue(value: AnyVal): ValueInfo =
            mockUnsafeMethod(value)
        }

        val a1: AnyVal = 333
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        variableInfoProfile.trySetValue(a1).get should be (r)
      }
    }

    describe("#setValue(AnyVal)") {
      it("should convert the local value to a remote value and set it") {
        val mockCreateRemotely = mockFunction[AnyVal, ValueInfo]
        val mockSetValueFromInfo = mockFunction[ValueInfo, ValueInfo]
        val variableInfoProfile = new TestVariableInfo {
          override def createRemotely(value: AnyVal): ValueInfo =
            mockCreateRemotely(value)

          override def setValueFromInfo(
            valueInfo: ValueInfo
          ): ValueInfo = mockSetValueFromInfo(valueInfo)
        }

        val a1: AnyVal = 33
        val r = mock[ValueInfo]
        mockCreateRemotely.expects(a1).returning(r).once()
        mockSetValueFromInfo.expects(r).returning(r).once()
        variableInfoProfile.setValue(a1) should be (r)
      }    }

    describe("#trySetValue(String)") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String, ValueInfo]

        val variableInfoProfile = new TestVariableInfo {
          override def setValue(value: String): ValueInfo =
            mockUnsafeMethod(value)
        }

        val a1 = "some string"
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        variableInfoProfile.trySetValue(a1).get should be (r)
      }
    }

    describe("#setValue(String)") {
      it("should convert the local value to a remote value and set it") {
        val mockCreateRemotely = mockFunction[String, ValueInfo]
        val mockSetValueFromInfo = mockFunction[ValueInfo, ValueInfo]
        val variableInfoProfile = new TestVariableInfo {
          override def createRemotely(value: String): ValueInfo =
            mockCreateRemotely(value)

          override def setValueFromInfo(
            valueInfo: ValueInfo
          ): ValueInfo = mockSetValueFromInfo(valueInfo)
        }

        val a1 = "some string"
        val r = mock[ValueInfo]
        mockCreateRemotely.expects(a1).returning(r).once()
        mockSetValueFromInfo.expects(r).returning(r).once()
        variableInfoProfile.setValue(a1) should be (r)
      }
    }

    describe("#trySetValueFromInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ValueInfo, ValueInfo]

        val variableInfoProfile = new TestVariableInfo {
          override def setValueFromInfo(
            valueInfo: ValueInfo
          ): ValueInfo = mockUnsafeMethod(valueInfo)
        }

        val a1 = mock[ValueInfo]
        val r = mock[ValueInfo]
        mockUnsafeMethod.expects(a1).returning(r).once()
        variableInfoProfile.trySetValueFromInfo(a1).get should be (r)
      }
    }

    describe("#toPrettyString") {
      it("should display the variable name and pretty-stringed value") {
        val expected = "someName = someValue"

        val mockValueInfoProfile = mock[ValueInfo]
        (mockValueInfoProfile.toPrettyString _).expects()
          .returning("someValue").once()

        val variableInfoProfile = new TestVariableInfo {
          override def name: String = "someName"
          override def tryToValueInfo: Try[ValueInfo] =
            Success(mockValueInfoProfile)
        }

        val actual = variableInfoProfile.toPrettyString

        actual should be(expected)
      }

      it("should display ??? for the value if unavailable") {
        val expected = "someName = ???"

        val variableInfoProfile = new TestVariableInfo {
          override def name: String = "someName"
          override def tryToValueInfo: Try[ValueInfo] =
            Failure(new Throwable)
        }

        val actual = variableInfoProfile.toPrettyString

        actual should be(expected)
      }
    }

    describe("#toValueInfo") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[ValueInfo]

        val variableInfoProfile = new TestVariableInfo {
          override def toValueInfo: ValueInfo = mockUnsafeMethod()
        }

        val r = mock[ValueInfo]
        mockUnsafeMethod.expects().returning(r).once()
        variableInfoProfile.tryToValueInfo.get should be (r)
      }
    }
  }
}
