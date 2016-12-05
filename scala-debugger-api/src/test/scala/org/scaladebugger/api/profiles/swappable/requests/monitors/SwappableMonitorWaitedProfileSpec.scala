package org.scaladebugger.api.profiles.swappable.requests.monitors

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import test.RequestInfoBuilder

class SwappableMonitorWaitedProfileSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMonitorWaitedProfile") {
    describe("#monitorWaitedRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.monitorWaitedRequests _).expects().once()

        swappableDebugProfile.monitorWaitedRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.monitorWaitedRequests
        }
      }
    }

    describe("#removeMonitorWaitedRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newMonitorWaitedRequestInfo())
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeMonitorWaitedRequestWithArgs _)
          .expects(extraArguments)
          .returning(expected).once()

        val actual = swappableDebugProfile.removeMonitorWaitedRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeMonitorWaitedRequestWithArgs(
            extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllMonitorWaitedRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newMonitorWaitedRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllMonitorWaitedRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllMonitorWaitedRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllMonitorWaitedRequests()
        }
      }
    }

    describe("#isMonitorWaitedRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isMonitorWaitedRequestWithArgsPending _).expects(
          extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isMonitorWaitedRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isMonitorWaitedRequestWithArgsPending(
            extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateMonitorWaitedRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateMonitorWaitedRequestWithData _).expects(arguments).once()

        swappableDebugProfile.tryGetOrCreateMonitorWaitedRequestWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateMonitorWaitedRequestWithData(arguments: _*)
        }
      }
    }
  }
}
