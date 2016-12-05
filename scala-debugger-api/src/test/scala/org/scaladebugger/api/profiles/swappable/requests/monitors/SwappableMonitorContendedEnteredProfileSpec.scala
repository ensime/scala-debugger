package org.scaladebugger.api.profiles.swappable.requests.monitors

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import test.RequestInfoBuilder

class SwappableMonitorContendedEnteredProfileSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMonitorContendedEnteredProfile") {
    describe("#monitorContendedEnteredRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.monitorContendedEnteredRequests _).expects().once()

        swappableDebugProfile.monitorContendedEnteredRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.monitorContendedEnteredRequests
        }
      }
    }

    describe("#removeMonitorContendedEnteredRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newMonitorContendedEnteredRequestInfo())
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeMonitorContendedEnteredRequestWithArgs _)
          .expects(extraArguments)
          .returning(expected).once()

        val actual = swappableDebugProfile.removeMonitorContendedEnteredRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeMonitorContendedEnteredRequestWithArgs(
            extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllMonitorContendedEnteredRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newMonitorContendedEnteredRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllMonitorContendedEnteredRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllMonitorContendedEnteredRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllMonitorContendedEnteredRequests()
        }
      }
    }

    describe("#isMonitorContendedEnteredRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isMonitorContendedEnteredRequestWithArgsPending _)
          .expects(extraArguments)
          .returning(expected).once()

        val actual = swappableDebugProfile.isMonitorContendedEnteredRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isMonitorContendedEnteredRequestWithArgsPending(
            extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateMonitorContendedEnteredRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData _)
          .expects(arguments).once()

        swappableDebugProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateMonitorContendedEnteredRequestWithData(arguments: _*)
        }
      }
    }
  }
}
