package org.scaladebugger.api.profiles.swappable.requests.breakpoints
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, Matchers, FunSpec}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import test.RequestInfoBuilder

class SwappableBreakpointProfileSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableBreakpointProfile") {
    describe("#breakpointRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.breakpointRequests _).expects().once()

        swappableDebugProfile.breakpointRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.breakpointRequests
        }
      }
    }

    describe("#removeBreakpointRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newBreakpointRequestInfo())
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeBreakpointRequests _).expects(
          fileName, lineNumber
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeBreakpointRequests(
          fileName, lineNumber
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeBreakpointRequests(fileName, lineNumber)
        }
      }
    }

    describe("#removeBreakpointRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newBreakpointRequestInfo())
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeBreakpointRequestWithArgs _).expects(
          fileName, lineNumber, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeBreakpointRequestWithArgs(
          fileName, lineNumber, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeBreakpointRequestWithArgs(
            fileName, lineNumber, extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllBreakpointRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newBreakpointRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllBreakpointRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllBreakpointRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllBreakpointRequests()
        }
      }
    }

    describe("#isBreakpointRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isBreakpointRequestPending _).expects(
          fileName, lineNumber
        ).returning(expected).once()

        val actual = swappableDebugProfile.isBreakpointRequestPending(
          fileName, lineNumber
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isBreakpointRequestPending(fileName, lineNumber)
        }
      }
    }

    describe("#isBreakpointRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isBreakpointRequestWithArgsPending _).expects(
          fileName, lineNumber, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isBreakpointRequestWithArgsPending(
          fileName, lineNumber, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val fileName = "some/file/name.scala"
        val lineNumber = 999
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isBreakpointRequestWithArgsPending(
            fileName, lineNumber, extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateBreakpointRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val fileName = "some file"
        val lineNumber = 999
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateBreakpointRequestWithData _).expects(
          fileName,
          lineNumber,
          arguments
        ).once()

        swappableDebugProfile.tryGetOrCreateBreakpointRequestWithData(
          fileName,
          lineNumber,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val fileName = "some file"
        val lineNumber = 999
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateBreakpointRequestWithData(
            fileName,
            lineNumber,
            arguments: _*
          )
        }
      }
    }
  }
}
