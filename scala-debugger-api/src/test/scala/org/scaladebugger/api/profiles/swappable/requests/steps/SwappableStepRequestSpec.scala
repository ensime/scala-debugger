package org.scaladebugger.api.profiles.swappable.requests.steps

import org.scaladebugger.api.profiles.traits.info.ThreadInfo
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import test.RequestInfoBuilder

class SwappableStepRequestSpec extends test.ParallelMockFunSpec
{
  private val mockThreadInfoProfile = mock[ThreadInfo]
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableStepRequest") {
    describe("#stepRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepRequests _).expects().once()

        swappableDebugProfile.stepRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepRequests
        }
      }
    }

    describe("#removeStepRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newStepRequestInfo())
        val threadInfoProfile = mock[ThreadInfo]

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeStepRequests _).expects(
          threadInfoProfile
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeStepRequests(
          threadInfoProfile
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val threadInfoProfile = mock[ThreadInfo]
        val lineNumber = 999

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeStepRequests(threadInfoProfile)
        }
      }
    }

    describe("#removeStepRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newStepRequestInfo())
        val threadInfoProfile = mock[ThreadInfo]
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeStepRequestWithArgs _).expects(
          threadInfoProfile, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeStepRequestWithArgs(
          threadInfoProfile, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val threadInfoProfile = mock[ThreadInfo]
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeStepRequestWithArgs(
            threadInfoProfile, extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllStepRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newStepRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllStepRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllStepRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllStepRequests()
        }
      }
    }

    describe("#isStepRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val threadInfoProfile = mock[ThreadInfo]

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isStepRequestPending _).expects(
          threadInfoProfile
        ).returning(expected).once()

        val actual = swappableDebugProfile.isStepRequestPending(
          threadInfoProfile
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val threadInfoProfile = mock[ThreadInfo]
        val methodName = "someMethod"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isStepRequestPending(threadInfoProfile)
        }
      }
    }

    describe("#isStepRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val threadInfoProfile = mock[ThreadInfo]
        val methodName = "someMethod"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isStepRequestWithArgsPending _).expects(
          threadInfoProfile, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isStepRequestWithArgsPending(
          threadInfoProfile, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val threadInfoProfile = mock[ThreadInfo]
        val methodName = "someMethod"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isStepRequestWithArgsPending(
            threadInfoProfile, extraArguments: _*
          )
        }
      }
    }
    
    describe("#stepIntoLineWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepIntoLineWithData _)
          .expects(mockThreadInfoProfile, arguments).once()

        swappableDebugProfile.stepIntoLineWithData(
          mockThreadInfoProfile,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepIntoLineWithData(
            mockThreadInfoProfile,
            arguments: _*
          )
        }
      }
    }

    describe("#stepOverLineWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepOverLineWithData _)
          .expects(mockThreadInfoProfile, arguments).once()


        swappableDebugProfile.stepOverLineWithData(
          mockThreadInfoProfile,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepOverLineWithData(
            mockThreadInfoProfile,
            arguments: _*
          )
        }
      }
    }

    describe("#stepOutLineWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepOutLineWithData _)
          .expects(mockThreadInfoProfile, arguments).once()


        swappableDebugProfile.stepOutLineWithData(
          mockThreadInfoProfile,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepOutLineWithData(
            mockThreadInfoProfile,
            arguments: _*
          )
        }
      }
    }

    describe("#stepIntoMinWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepIntoMinWithData _)
          .expects(mockThreadInfoProfile, arguments).once()


        swappableDebugProfile.stepIntoMinWithData(
          mockThreadInfoProfile,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepIntoMinWithData(
            mockThreadInfoProfile,
            arguments: _*
          )
        }
      }
    }

    describe("#stepOverMinWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepOverMinWithData _)
          .expects(mockThreadInfoProfile, arguments).once()


        swappableDebugProfile.stepOverMinWithData(
          mockThreadInfoProfile,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepOverMinWithData(
            mockThreadInfoProfile,
            arguments: _*
          )
        }
      }
    }

    describe("#stepOutMinWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.stepOutMinWithData _)
          .expects(mockThreadInfoProfile, arguments).once()


        swappableDebugProfile.stepOutMinWithData(
          mockThreadInfoProfile,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.stepOutMinWithData(
            mockThreadInfoProfile,
            arguments: _*
          )
        }
      }
    }

    describe("#tryCreateStepListenerWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryCreateStepListenerWithData _)
          .expects(mockThreadInfoProfile, arguments).once()


        swappableDebugProfile.tryCreateStepListenerWithData(
          mockThreadInfoProfile,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryCreateStepListenerWithData(
            mockThreadInfoProfile,
            arguments: _*
          )
        }
      }
    }
  }
}
