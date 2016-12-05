package org.scaladebugger.api.profiles.swappable

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.traits.DebugProfile

class SwappableDebugProfileManagementSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableDebugProfileManagement") {
    describe("#use") {
      it("should set the current underlying profile") {
        val expected = mockDebugProfile
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name)
          .returning(Some(expected)).once()

        swappableDebugProfile.use(name)

        val actual = swappableDebugProfile.withCurrentProfile

        actual should be (expected)
      }
    }

    describe("#withCurrentProfile") {
      it("should return the currently-active profile") {
        val expected = mockDebugProfile
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name)
          .returning(Some(expected)).once()

        swappableDebugProfile.use(name)

        val actual = swappableDebugProfile.withCurrentProfile

        actual should be (expected)
      }

      it("should throw an exception if the profile is not found") {
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name).returning(None).once()

        swappableDebugProfile.use(name)

        intercept[AssertionError] {
          swappableDebugProfile.withCurrentProfile
        }
      }
    }

    describe("#withProfile") {
      it("should return the profile with the specified name") {
        val expected = mockDebugProfile
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name)
          .returning(Some(expected)).once()

        val actual = swappableDebugProfile.withProfile(name)

        actual should be (expected)
      }

      it("should throw an exception if the profile is not found") {
        val name = "some name"

        (mockProfileManager.retrieve _).expects(name).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.withProfile(name)
        }
      }
    }
  }
}
