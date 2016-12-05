package org.scaladebugger.api.profiles.swappable.info
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.profiles.traits.info.ValueInfoProfile

class SwappableMiscInfoProfileSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableMiscInfoProfile") {
    describe("#availableLinesForFile") {
      it("should invoke the method on the underlying profile") {
        val expected = Some(Seq(1, 2, 3))
        val fileName = "some file"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.availableLinesForFile _).expects(fileName)
          .returning(expected).once()

        val actual = swappableDebugProfile.availableLinesForFile(fileName)

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val fileName = "some file"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.availableLinesForFile(fileName)
        }
      }
    }

    describe("#createRemotely(AnyVal)") {
      it("should invoke the method on the underlying profile") {
        val expected = mock[ValueInfoProfile]
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
        val expected = mock[ValueInfoProfile]
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

    describe("#sourceNameToPaths") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq("path/to/file.scala")
        val sourceName = "file.scala"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.sourceNameToPaths _).expects(sourceName)
          .returning(expected).once()

        val actual = swappableDebugProfile.sourceNameToPaths(sourceName)

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        val sourceName = "file.scala"

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.availableLinesForFile(sourceName)
        }
      }
    }

    describe("#mainClassName") {
      it("should invoke the method on the underlying profile") {
        val expected = "some class name"

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.mainClassName _).expects().returning(expected).once()

        val actual = swappableDebugProfile.mainClassName

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.mainClassName
        }
      }
    }

    describe("#commandLineArguments") {
      it("should invoke the method on the underlying profile") {
        val expected = Seq("some", "arguments")

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.commandLineArguments _).expects()
          .returning(expected).once()

        val actual = swappableDebugProfile.commandLineArguments

        actual should be (expected)
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.mainClassName
        }
      }
    }
  }
}
