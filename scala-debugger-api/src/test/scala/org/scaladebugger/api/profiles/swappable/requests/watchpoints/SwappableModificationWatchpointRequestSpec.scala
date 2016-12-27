package org.scaladebugger.api.profiles.swappable.requests.watchpoints

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.RequestInfoBuilder

class SwappableModificationWatchpointRequestSpec extends ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableModificationWatchpointRequest") {
    describe("#modificationWatchpointRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.modificationWatchpointRequests _).expects().once()

        swappableDebugProfile.modificationWatchpointRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.modificationWatchpointRequests
        }
      }
    }

    describe("#removeModificationWatchpointRequests") {
      it("should invoke the field on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newModificationWatchpointRequestInfo())
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeModificationWatchpointRequests _).expects(
          className, fieldName
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeModificationWatchpointRequests(
          className, fieldName
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val className = "some.class.name"
        val fieldName = "someFieldName"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeModificationWatchpointRequests(className, fieldName)
        }
      }
    }

    describe("#removeModificationWatchpointRequestWithArgs") {
      it("should invoke the field on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newModificationWatchpointRequestInfo())
        val className = "some.class.name"
        val fieldName = "someFieldName"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeModificationWatchpointRequestWithArgs _).expects(
          className, fieldName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeModificationWatchpointRequestWithArgs(
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
          swappableDebugProfile.removeModificationWatchpointRequestWithArgs(
            className, fieldName, extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllModificationWatchpointRequests") {
      it("should invoke the field on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newModificationWatchpointRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllModificationWatchpointRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllModificationWatchpointRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllModificationWatchpointRequests()
        }
      }
    }

    describe("#isModificationWatchpointRequestPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val fieldName = "someField"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isModificationWatchpointRequestPending _).expects(
          className, fieldName
        ).returning(expected).once()

        val actual = swappableDebugProfile.isModificationWatchpointRequestPending(
          className, fieldName
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val className = "some.class.name"
        val fieldName = "someField"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isModificationWatchpointRequestPending(
            className,
            fieldName
          )
        }
      }
    }

    describe("#isModificationWatchpointRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val className = "some.class.name"
        val fieldName = "someField"
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isModificationWatchpointRequestWithArgsPending _).expects(
          className, fieldName, extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isModificationWatchpointRequestWithArgsPending(
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
          swappableDebugProfile.isModificationWatchpointRequestWithArgsPending(
            className, fieldName, extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateModificationWatchpointRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val className = "some class"
        val fieldName = "some field"
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        // NOTE: Forced to use onCall with product due to issues with ScalaMock
        //       casting and inability to work with varargs directly
        (mockDebugProfile.tryGetOrCreateModificationWatchpointRequestWithData(
          _: String,
          _: String,
          _: JDIArgument)
        ).expects(className, fieldName, *).onCall(t => {
          val args = t.productElement(2).asInstanceOf[Seq[JDIArgument]]
          args should be (arguments)
          null
        })

        swappableDebugProfile.tryGetOrCreateModificationWatchpointRequestWithData(
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
          swappableDebugProfile.tryGetOrCreateModificationWatchpointRequestWithData(
            className,
            fieldName,
            arguments: _*
          )
        }
      }
    }
  }
}
