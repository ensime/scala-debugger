package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestStringInfoProfile

class StringInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  describe("StringInfoProfile") {
    describe("#toPrettyString") {
      it("should return the local string value wrapped in double quotes") {
        val q = '"'
        val expected = s"${q}TEST${q}"

        val stringInfoProfile = new TestStringInfoProfile {
          override def toLocalValue: Any = "TEST"
        }

        val actual = stringInfoProfile.toPrettyString

        actual should be(expected)
      }
    }
  }
}
