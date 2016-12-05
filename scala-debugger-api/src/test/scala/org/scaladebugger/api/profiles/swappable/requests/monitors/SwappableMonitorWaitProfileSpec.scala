package org.scaladebugger.api.profiles.swappable.requests.monitors

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import test.RequestInfoBuilder

class SwappableMonitorWaitProfileSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMonitorWaitProfile") {
    describe("#monitorWaitRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.monitorWaitRequests _).expects().once()

        swappableDebugProfile.monitorWaitRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.monitorWaitRequests
        }
      }
    }

    describe("#removeMonitorWaitRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newMonitorWaitRequestInfo())
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeMonitorWaitRequestWithArgs _)
          .expects(extraArguments)
          .returning(expected).once()

        val actual = swappableDebugProfile.removeMonitorWaitRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeMonitorWaitRequestWithArgs(
            extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllMonitorWaitRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newMonitorWaitRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllMonitorWaitRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllMonitorWaitRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllMonitorWaitRequests()
        }
      }
    }

    describe("#isMonitorWaitRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isMonitorWaitRequestWithArgsPending _).expects(
          extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isMonitorWaitRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isMonitorWaitRequestWithArgsPending(
            extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateMonitorWaitRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateMonitorWaitRequestWithData _).expects(arguments).once()

        swappableDebugProfile.tryGetOrCreateMonitorWaitRequestWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateMonitorWaitRequestWithData(arguments: _*)
        }
      }
    }
  }
}
