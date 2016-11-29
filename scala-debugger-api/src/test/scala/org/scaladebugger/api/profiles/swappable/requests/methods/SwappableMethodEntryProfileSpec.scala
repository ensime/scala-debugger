package org.scaladebugger.api.profiles.swappable.requests.methods
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import test.RequestInfoBuilder

class SwappableMethodEntryProfileSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMethodEntryProfile") {
    describe("#methodEntryRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.methodEntryRequests _).expects().once()

        swappableDebugProfile.methodEntryRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.methodEntryRequests
        }
      }
    }

    describe("#removeMethodEntryRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newMethodEntryRequestInfo())
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeMethodEntryRequests _).expects(
          className, methodName
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeMethodEntryRequests(
          className, methodName
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val className = "some.class.name"
        val methodName = "someMethodName"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeMethodEntryRequests(className, methodName)
        }
      }
    }

    describe("#removeMethodEntryRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newMethodEntryRequestInfo())
        val className = "some.class.name"
        val methodName = "someMethodName"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeMethodEntryRequestWithArgs _).expects(
          className, methodName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeMethodEntryRequestWithArgs(
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
          swappableDebugProfile.removeMethodEntryRequestWithArgs(
            className, methodName, extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllMethodEntryRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newMethodEntryRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllMethodEntryRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllMethodEntryRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllMethodEntryRequests()
        }
      }
    }

    describe("#isMethodEntryRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethod"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isMethodEntryRequestPending _).expects(
          className, methodName
        ).returning(expected).once()

        val actual = swappableDebugProfile.isMethodEntryRequestPending(
          className, methodName
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some.class.name"
        val methodName = "someMethod"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isMethodEntryRequestPending(
            className,
            methodName
          )
        }
      }
    }

    describe("#isMethodEntryRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val methodName = "someMethod"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isMethodEntryRequestWithArgsPending _).expects(
          className, methodName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isMethodEntryRequestWithArgsPending(
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
          swappableDebugProfile.isMethodEntryRequestWithArgsPending(
            className, methodName, extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateMethodEntryRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val className = "some class"
        val methodName = "some method"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateMethodEntryRequestWithData _)
          .expects(className, methodName, arguments).once()

        swappableDebugProfile.tryGetOrCreateMethodEntryRequestWithData(
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
          swappableDebugProfile.tryGetOrCreateMethodEntryRequestWithData(
            className,
            methodName,
            arguments: _*
          )
        }
      }
    }
  }
}
