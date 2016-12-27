package org.scaladebugger.api.lowlevel.events.misc

import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class YesResumeSpec extends ParallelMockFunSpec
{
  describe("YesResume") {
    describe("#value") {
      it("should return true") {
        val expected = true
        val actual = YesResume.value
        actual should be (expected)
      }
    }

    describe("#toProcessor") {
      it("should return a processor containing YesResume") {
        YesResume.toProcessor.argument should be (YesResume)
      }
    }
  }
}

