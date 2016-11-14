package org.scaladebugger.api.lowlevel.events.misc
import acyclic.file

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, Matchers, FunSpec}

class NoResumeSpec extends test.ParallelMockFunSpec
{
  describe("NoResume") {
    describe("#value") {
      it("should return false") {
        val expected = false
        val actual = NoResume.value
        actual should be (expected)
      }
    }

    describe("#toProcessor") {
      it("should return a processor containing NoResume") {
        NoResume.toProcessor.argument should be (NoResume)
      }
    }
  }
}

