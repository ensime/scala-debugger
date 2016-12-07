package org.scaladebugger.api.profiles.swappable.requests.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import test.RequestInfoBuilder

class SwappableAccessWatchpointRequestSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableAccessWatchpointRequest") {
    describe("#accessWatchpointRequests") {
      it("should invoke the field on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.accessWatchpointRequests _).expects().once()

        swappableDebugProfile.accessWatchpointRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.accessWatchpointRequests
        }
      }
    }

    describe("#removeAccessWatchpointRequests") {
      it("should invoke the field on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newAccessWatchpointRequestInfo())
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAccessWatchpointRequests _).expects(
          className, fieldName
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeAccessWatchpointRequests(
          className, fieldName
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAccessWatchpointRequests(className, fieldName)
        }
      }
    }

    describe("#removeAccessWatchpointRequestWithArgs") {
      it("should invoke the field on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newAccessWatchpointRequestInfo())
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAccessWatchpointRequestWithArgs _).expects(
          className, fieldName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeAccessWatchpointRequestWithArgs(
          className, fieldName, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAccessWatchpointRequestWithArgs(
            className, fieldName, extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllAccessWatchpointRequests") {
      it("should invoke the field on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newAccessWatchpointRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllAccessWatchpointRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllAccessWatchpointRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllAccessWatchpointRequests()
        }
      }
    }

    describe("#isAccessWatchpointRequestPending") {
      it("should invoke the field on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val fieldName = "someField"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isAccessWatchpointRequestPending _).expects(
          className, fieldName
        ).returning(expected).once()

        val actual = swappableDebugProfile.isAccessWatchpointRequestPending(
          className, fieldName
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some.class.name"
        val fieldName = "someField"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isAccessWatchpointRequestPending(
            className,
            fieldName
          )
        }
      }
    }

    describe("#isAccessWatchpointRequestWithArgsPending") {
      it("should invoke the field on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val fieldName = "someField"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isAccessWatchpointRequestWithArgsPending _).expects(
          className, fieldName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isAccessWatchpointRequestWithArgsPending(
          className, fieldName, extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some.class.name"
        val fieldName = "someField"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isAccessWatchpointRequestWithArgsPending(
            className, fieldName, extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateAccessWatchpointRequestWithData") {
      it("should invoke the field on the underlying profile") {
        val className = "some class"
        val fieldName = "some field"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        // NOTE: Forced to use onCall with product due to issues with ScalaMock
        //       casting and inability to work with varargs directly
        (mockDebugProfile.tryGetOrCreateAccessWatchpointRequestWithData(
          _: String,
          _: String,
          _: JDIArgument)
        ).expects(className, fieldName, *).onCall(t => {
          val args = t.productElement(2).asInstanceOf[Seq[JDIArgument]]
          args should be (arguments)
          null
        })

        swappableDebugProfile.tryGetOrCreateAccessWatchpointRequestWithData(
          className,
          fieldName,
          arguments: _*
        )
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some class"
        val fieldName = "some field"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateAccessWatchpointRequestWithData(
            className,
            fieldName,
            arguments: _*
          )
        }
      }
    }
  }
}
