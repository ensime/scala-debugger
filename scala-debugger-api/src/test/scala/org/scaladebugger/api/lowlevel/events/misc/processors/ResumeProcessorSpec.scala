package org.scaladebugger.api.lowlevel.events.misc.processors

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.misc.{NoResume, YesResume}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, Matchers, FunSpec}

class ResumeProcessorSpec extends test.ParallelMockFunSpec
{
  describe("ResumeProcessor") {
    describe("#process") {
      it("should return false if created from NoResume") {
        val expected = false
        val actual = NoResume.toProcessor.process(mock[Event])
        actual should be (expected)
      }

      it("should return true if created from YesResume") {
        val expected = true
        val actual = YesResume.toProcessor.process(mock[Event])
        actual should be (expected)
      }
    }
  }
}
