package org.scaladebugger.api.lowlevel.events.filters.processors

import com.sun.jdi.event.Event
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.filters.MaxTriggerFilter
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class MaxTriggerFilterProcessorSpec extends ParallelMockFunSpec
{
  private val testCount = 3
  private val maxTriggerFilter = MaxTriggerFilter(count = testCount)
  private val maxTriggerProcessor =
    new MaxTriggerFilterProcessor(maxTriggerFilter)

  describe("MaxTriggerFilterProcessor") {
    describe("#process") {
      it ("should return true if the total processed has not exceeded the max count") {
        // Verify that the processor is true for the first N processes
        for (i <- 1 to testCount) {
          maxTriggerProcessor.process(mock[Event]) should be (true)
        }
      }

      it("should return false if the total processed has exceeded the max count") {
        for (i <- 1 to testCount) {
          maxTriggerProcessor.process(mock[Event])
        }

        // Verify that the processor is false after the first N processes
        maxTriggerProcessor.process(mock[Event]) should be (false)
      }
    }

    describe("#reset") {
      it("should reset the internal counter to zero") {
        for (i <- 1 to testCount) {
          maxTriggerProcessor.process(mock[Event])
        }

        maxTriggerProcessor.reset()

        // Verify that the processor is true even after the first N processes
        maxTriggerProcessor.process(mock[Event]) should be (true)
      }
    }
  }
}
