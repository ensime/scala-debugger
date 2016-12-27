package org.scaladebugger.api.profiles

import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class StandardProfileManagerSpec extends ParallelMockFunSpec {

  private val profileManager = new StandardProfileManager

  describe("StandardProfileManager") {
    describe("#register") {
      it("should return Some(profile) if the profile was registered") {
        val expected = Some(mock[DebugProfile])
        val name = "some name"

        val actual = profileManager.register(name, expected.get)

        actual should be (expected)
      }

      it("should return None if the name is already in use") {
        val expected = None
        val name = "some name"

        profileManager.register(name, mock[DebugProfile])

        val actual = profileManager.register(name, mock[DebugProfile])

        actual should be (expected)
      }

      it("should register the profile if the name has not been used") {
        val expected = Some(mock[DebugProfile])
        val name = "some name"

        profileManager.register(name, expected.get)

        val actual = profileManager.retrieve(name)

        actual should be (expected)
      }

      it("should not register the profile if the name has already been used") {
        val expected = Some(mock[DebugProfile])
        val name = "some name"

        profileManager.register(name, expected.get)
        profileManager.register(name, mock[DebugProfile])

        val actual = profileManager.retrieve(name)

        actual should be (expected)
      }

      it("should throw an exception if a null name is provided") {
        intercept[IllegalArgumentException] {
          profileManager.register(null, mock[DebugProfile])
        }
      }

      it("should throw an exception if an empty name is provided") {
        intercept[IllegalArgumentException] {
          profileManager.register("", mock[DebugProfile])
        }
      }

      it("should throw an exception if a null profile is provided") {
        intercept[IllegalArgumentException] {
          profileManager.register("some name", null)
        }
      }
    }

    describe("#unregister") {
      it("should remove the profile with the matching name") {
        val expected = None
        val name = "some name"

        profileManager.register(name, mock[DebugProfile])
        profileManager.unregister(name)

        val actual = profileManager.retrieve(name)

        actual should be (expected)
      }

      it("should return Some(profile) if a matching profile was found") {
        val expected = Some(mock[DebugProfile])
        val name = "some name"

        profileManager.register(name, expected.get)
        val actual = profileManager.unregister(name)

        actual should be (expected)
      }

      it("should return None if no profile matches the provided name") {
        val expected = None
        val name = "some name"

        val actual = profileManager.unregister(name)

        actual should be (expected)
      }
    }

    describe("#retrieve") {
      it("should return Some(profile) if a profile is found") {
        val expected = Some(mock[DebugProfile])
        val name = "some name"

        profileManager.register(name, expected.get)
        val actual = profileManager.retrieve(name)

        actual should be (expected)
      }

      it("should return None if no profile is found") {
        val expected = None

        val actual = profileManager.retrieve("some name")

        actual should be (expected)
      }
    }
  }
}
