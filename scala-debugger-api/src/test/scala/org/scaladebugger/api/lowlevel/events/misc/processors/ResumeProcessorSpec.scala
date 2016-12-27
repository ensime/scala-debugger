package org.scaladebugger.api.lowlevel.events.misc.processors

import com.sun.jdi.event.Event
import org.scaladebugger.api.lowlevel.events.misc.{NoResume, YesResume}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class ResumeProcessorSpec extends ParallelMockFunSpec
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
