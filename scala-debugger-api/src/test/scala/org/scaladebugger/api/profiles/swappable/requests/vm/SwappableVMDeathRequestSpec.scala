package org.scaladebugger.api.profiles.swappable.requests.vm

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.RequestInfoBuilder

class SwappableVMDeathRequestSpec extends ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableVMDeathRequest") {
    describe("#vmDeathRequests") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.vmDeathRequests _).expects().once()

        swappableDebugProfile.vmDeathRequests
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.vmDeathRequests
        }
      }
    }

    describe("#removeVMDeathRequestWithArgs") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(RequestInfoBuilder.newVMDeathRequestInfo())
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeVMDeathRequestWithArgs _).expects(
          extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.removeVMDeathRequestWithArgs(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeVMDeathRequestWithArgs(
            extraArguments: _*
          )
        }
      }
    }

    describe("#removeAllVMDeathRequests") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq(RequestInfoBuilder.newVMDeathRequestInfo())

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.removeAllVMDeathRequests _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.removeAllVMDeathRequests()

        actual should be (expected)
      }

      it("should throw an exception if there remove no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.removeAllVMDeathRequests()
        }
      }
    }

    describe("#isVMDeathRequestWithArgsPending") {
      it("should invoke the method on the underlying profile") {
        val expected = true
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.isVMDeathRequestWithArgsPending _).expects(
          extraArguments
        ).returning(expected).once()

        val actual = swappableDebugProfile.isVMDeathRequestWithArgsPending(
          extraArguments: _*
        )

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val extraArguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.isVMDeathRequestWithArgsPending(
            extraArguments: _*
          )
        }
      }
    }

    describe("#tryGetOrCreateVMDeathRequestWithData") {
      it("should invoke the method on the underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryGetOrCreateVMDeathRequestWithData _).expects(arguments).once()

        swappableDebugProfile.tryGetOrCreateVMDeathRequestWithData(arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryGetOrCreateVMDeathRequestWithData(arguments: _*)
        }
      }
    }
  }
}
