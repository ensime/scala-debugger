package org.scaladebugger.api.profiles.swappable.requests.exceptions

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.RequestInfoBuilder

class SwappableExceptionRequestSpec extends ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableExceptionRequest") {
    describe("#exceptionRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.exceptionRequests _).expects().once()

        swappableDebugProfile.exceptionRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.exceptionRequests
        }
      }
    }

    describe("#removeOnlyAllExceptionsRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newExceptionRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeOnlyAllExceptionsRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeOnlyAllExceptionsRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeOnlyAllExceptionsRequests()
        }
      }
    }

    describe("#removeOnlyAllExceptionsRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newExceptionRequestInfo())
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeOnlyAllExceptionsRequestWithArgs _).expects(
          notifyCaught, notifyUncaught, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeOnlyAllExceptionsRequestWithArgs(
          notifyCaught, notifyUncaught, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeOnlyAllExceptionsRequestWithArgs(
            notifyCaught, notifyUncaught, extraArguments: _*
          )
        }
      }
    }

    describe("#removeExceptionRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newExceptionRequestInfo())
        val exceptionName = "some.exception.name"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeExceptionRequests _).expects(
          exceptionName
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeExceptionRequests(
          exceptionName
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val exceptionName = "some.exception.name"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeExceptionRequests(exceptionName)
        }
      }
    }

    describe("#removeExceptionRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newExceptionRequestInfo())
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeExceptionRequestWithArgs _).expects(
          exceptionName, notifyCaught, notifyUncaught, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeExceptionRequestWithArgs(
          exceptionName, notifyCaught, notifyUncaught, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val exceptionName = "some.exception.name"
        val notifyCaught = true
        val notifyUncaught = false
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeExceptionRequestWithArgs(
            exceptionName, notifyCaught, notifyUncaught, extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllExceptionRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newExceptionRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllExceptionRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllExceptionRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllExceptionRequests()
        }
      }
    }

    describe("#isExceptionRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        val exceptionName = "some exception"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isExceptionRequestPending _).expects(
          exceptionName
        ).returning(expected).once()

        val actual = swappableDebugProfile.isExceptionRequestPending(
          exceptionName
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val exceptionName = "some exception"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isExceptionRequestPending(exceptionName)
        }
      }
    }

    describe("#isExceptionRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isExceptionRequestWithArgsPending _).expects(
          exceptionName, notifyCaught, notifyUncaught, arguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isExceptionRequestWithArgsPending(
          exceptionName, notifyCaught, notifyUncaught, arguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isExceptionRequestWithArgsPending(
            exceptionName, notifyCaught, notifyUncaught, arguments: _*
          )
        }
      }
    }

    describe("#isAllExceptionsRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isAllExceptionsRequestPending _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.isAllExceptionsRequestPending

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isAllExceptionsRequestPending
        }
      }
    }

    describe("#isAllExceptionsRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true

        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isAllExceptionsRequestWithArgsPending _).expects(
          notifyCaught, notifyUncaught, arguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isAllExceptionsRequestWithArgsPending(
          notifyCaught, notifyUncaught, arguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isAllExceptionsRequestWithArgsPending(
            notifyCaught, notifyUncaught, arguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateExceptionRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateExceptionRequestWithData _).expects(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments
        ).once()

        swappableDebugProfile.tryGetOrCreateExceptionRequestWithData(
          exceptionName,
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val exceptionName = "some exception"
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateExceptionRequestWithData(
            exceptionName,
            notifyCaught,
            notifyUncaught,
            arguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateAllExceptionsRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateAllExceptionsRequestWithData _).expects(
          notifyCaught,
          notifyUncaught,
          arguments
        ).once()

        swappableDebugProfile.tryGetOrCreateAllExceptionsRequestWithData(
          notifyCaught,
          notifyUncaught,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val notifyCaught = true
        val notifyUncaught = true
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateAllExceptionsRequestWithData(
            notifyCaught,
            notifyUncaught,
            arguments: _*
          )
        }
      }
    }
  }
}
