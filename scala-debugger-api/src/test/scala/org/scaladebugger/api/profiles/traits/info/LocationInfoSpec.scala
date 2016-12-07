package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestLocationInfo

import scala.util.{Failure, Success, Try}

class LocationInfoSpec extends test.ParallelMockFunSpec
{
  describe("LocationInfo") {
    describe("#toPrettyString") {
      it("should include the source path if available") {
        val expected = "path/to/file.scala : 999"

        val locationInfoProfile = new TestLocationInfo {
          override def trySourcePath: Try[String] =
            Success("path/to/file.scala")
          override def lineNumber: Int = 999
        }

        val actual = locationInfoProfile.toPrettyString

        actual should be (expected)
      }

      it("should use ??? for the source path if unavailable") {
        val expected = "??? : 999"

        val locationInfoProfile = new TestLocationInfo {
          override def trySourcePath: Try[String] = Failure(new Throwable)
          override def lineNumber: Int = 999
        }

        val actual = locationInfoProfile.toPrettyString

        actual should be (expected)
      }
    }

    describe("#codeIndexOption") {
      it("should return Some(code position) if position is 0 or greater") {
        val expected = Some(999)

        val locationInfoProfile = new TestLocationInfo {
          override def codeIndex: Long = expected.get
        }

        val actual = locationInfoProfile.codeIndexOption

        actual should be (expected)
      }

      it("should return None if position is less than 0") {
        val expected = None

        val locationInfoProfile = new TestLocationInfo {
          override def codeIndex: Long = -1
        }

        val actual = locationInfoProfile.codeIndexOption

        actual should be (expected)
      }
    }

    describe("#lineNumberOption") {
      it("should return Some(line number) if line is 0 or greater") {
        val expected = Some(999)

        val locationInfoProfile = new TestLocationInfo {
          override def lineNumber: Int = expected.get
        }

        val actual = locationInfoProfile.lineNumberOption

        actual should be (expected)
      }

      it("should return None if line is less than 0") {
        val expected = None

        val locationInfoProfile = new TestLocationInfo {
          override def lineNumber: Int = -1
        }

        val actual = locationInfoProfile.lineNumberOption

        actual should be (expected)
      }
    }
  }
}
