package org.scaladebugger.api.profiles.swappable.requests.methods

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.RequestInfoBuilder

class SwappableMethodExitRequestSpec extends ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMethodExitRequest") {
    describe("#methodExitRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.methodExitRequests _).expects().once()

        swappableDebugProfile.methodExitRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.methodExitRequests
        }
      }
    }

    describe("#removeMethodExitRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newMethodExitRequestInfo())
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeMethodExitRequests _).expects(
          className, methodName
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeMethodExitRequests(
          className, methodName
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeMethodExitRequests(className, methodName)
        }
      }
    }

    describe("#removeMethodExitRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newMethodExitRequestInfo())
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeMethodExitRequestWithArgs _).expects(
          className, methodName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeMethodExitRequestWithArgs(
          className, methodName, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeMethodExitRequestWithArgs(
            className, methodName, extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllMethodExitRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newMethodExitRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllMethodExitRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllMethodExitRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllMethodExitRequests()
        }
      }
    }

    describe("#isMethodExitRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethod"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isMethodExitRequestPending _).expects(
          className, methodName
        ).returning(expected).once()

        val actual = swappableDebugProfile.isMethodExitRequestPending(
          className, methodName
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some.class.name"
        val methodName = "someMethod"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isMethodExitRequestPending(
            className,
            methodName
          )
        }
      }
    }

    describe("#isMethodExitRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethod"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isMethodExitRequestWithArgsPending _).expects(
          className, methodName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isMethodExitRequestWithArgsPending(
          className, methodName, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some.class.name"
        val methodName = "someMethod"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isMethodExitRequestWithArgsPending(
            className, methodName, extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateMethodExitRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val className = "some class"
        val methodName = "some method"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateMethodExitRequestWithData _)
          .expects(className, methodName, arguments).once()

        swappableDebugProfile.tryGetOrCreateMethodExitRequestWithData(
          className,
          methodName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some class"
        val methodName = "some method"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateMethodExitRequestWithData(
            className,
            methodName,
            arguments: _*
          )
        }
      }
    }
  }
}
