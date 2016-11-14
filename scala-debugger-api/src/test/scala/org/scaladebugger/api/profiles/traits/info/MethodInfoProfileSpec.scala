package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestMethodInfoProfile

import scala.util.{Failure, Success, Try}

class MethodInfoProfileSpec extends test.ParallelMockFunSpec
{
  describe("MethodInfoProfile") {
    describe("#toPrettyString") {
      it("should display the name, type parameters, and return type of the method") {
        val expected = "def methodName(type1,type2): returnType"

        val methodInfoProfile = new TestMethodInfoProfile {
          override def name: String = "methodName"
          override def tryParameterTypeNames: Try[Seq[String]] =
            Success(Seq("type1", "type2"))
          override def tryReturnTypeName: Try[String] = Success("returnType")
        }

        val actual = methodInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should display ??? for the type parameters if unavailable") {
        val expected = "def methodName(???): returnType"

        val methodInfoProfile = new TestMethodInfoProfile {
          override def name: String = "methodName"
          override def tryParameterTypeNames: Try[Seq[String]] =
            Failure(new Throwable)
          override def tryReturnTypeName: Try[String] = Success("returnType")
        }

        val actual = methodInfoProfile.toPrettyString

        actual should be (expected)      }

      it("should display ??? for the return type if unavailable") {
        val expected = "def methodName(type1,type2): ???"

        val methodInfoProfile = new TestMethodInfoProfile {
          override def name: String = "methodName"
          override def tryParameterTypeNames: Try[Seq[String]] =
            Success(Seq("type1", "type2"))
          override def tryReturnTypeName: Try[String] =
            Failure(new Throwable)
        }

        val actual = methodInfoProfile.toPrettyString

        actual should be (expected)      }
    }

    describe("#tryReturnTypeName") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[String]

        val methodInfoProfile = new TestMethodInfoProfile {
          override def returnTypeName: String = mockUnsafeMethod()
        }

        val r = "some.return.type"
        mockUnsafeMethod.expects().returning(r).once()
        methodInfoProfile.tryReturnTypeName.get should be (r)
      }
    }

    describe("#tryParameterTypeNames") {
      it("should wrap the unsafe call in a Try") {
        val mockUnsafeMethod = mockFunction[Seq[String]]

        val methodInfoProfile = new TestMethodInfoProfile {
          override def parameterTypeNames: Seq[String] = mockUnsafeMethod()
        }

        val r = Seq("some.param.type")
        mockUnsafeMethod.expects().returning(r).once()
        methodInfoProfile.tryParameterTypeNames.get should be (r)
      }
    }
  }
}
