package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestStringInfo

class StringInfoSpec extends test.ParallelMockFunSpec
{
  describe("StringInfo") {
    describe("#toPrettyString") {
      it("should return the local string value wrapped in double quotes") {
        val q = '"'
        val expected = s"${q}TEST${q}"

        val stringInfoProfile = new TestStringInfo {
          override def toLocalValue: Any = "TEST"
        }

        val actual = stringInfoProfile.toPrettyString

        actual should be(expected)
      }
    }
  }
}
