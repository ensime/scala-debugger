package org.scaladebugger.api.profiles.swappable.requests.classes

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.RequestInfoBuilder

class SwappableClassPrepareRequestSpec extends ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableClassPrepareRequest") {
    describe("#classPrepareRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.classPrepareRequests _).expects().once()

        swappableDebugProfile.classPrepareRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.classPrepareRequests
        }
      }
    }

    describe("#removeClassPrepareRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newClassPrepareRequestInfo())
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeClassPrepareRequestWithArgs _).expects(
          extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeClassPrepareRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeClassPrepareRequestWithArgs(
            extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllClassPrepareRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newClassPrepareRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllClassPrepareRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllClassPrepareRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllClassPrepareRequests()
        }
      }
    }

    describe("#isClassPrepareRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isClassPrepareRequestWithArgsPending _).expects(
          extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isClassPrepareRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isClassPrepareRequestWithArgsPending(
            extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateClassPrepareRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateClassPrepareRequestWithData _).expects(arguments).once()

        swappableDebugProfile.tryGetOrCreateClassPrepareRequestWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateClassPrepareRequestWithData(arguments: _*)
        }
      }
    }
  }
}
