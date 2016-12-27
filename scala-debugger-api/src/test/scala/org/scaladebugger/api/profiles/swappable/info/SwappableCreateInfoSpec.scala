package org.scaladebugger.api.profiles.swappable.info

import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.profiles.traits.info.ValueInfo
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class SwappableCreateInfoSpec extends ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMiscInfo") {
    describe("#createRemotely(AnyVal)") {
      it("should invoke the method on the underlying profile") {
        val expected = mock[ValueInfo]
        val value: AnyVal = 33

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.createRemotely(_: AnyVal)).expects(value)
          .returning(expected).once()

        val actual = swappableDebugProfile.createRemotely(value)

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val value = "some string"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.createRemotely(value)
        }
      }
    }

    describe("#createRemotely(String)") {
      it("should invoke the method on the underlying profile") {
        val expected = mock[ValueInfo]
        val value = "some string"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.createRemotely(_: String)).expects(value)
          .returning(expected).once()

        val actual = swappableDebugProfile.createRemotely(value)

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val value = "some string"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.createRemotely(value)
        }
      }
    }
  }
}
