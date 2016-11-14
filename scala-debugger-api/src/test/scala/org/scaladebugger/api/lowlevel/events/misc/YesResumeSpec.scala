package org.scaladebugger.api.lowlevel.events.misc
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class YesResumeSpec extends test.ParallelMockFunSpec
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

