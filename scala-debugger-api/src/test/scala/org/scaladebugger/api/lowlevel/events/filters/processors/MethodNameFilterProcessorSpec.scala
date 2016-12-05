package org.scaladebugger.api.lowlevel.events.filters.processors

import com.sun.jdi.{Location, Method}
import com.sun.jdi.event.{Event, LocatableEvent, MethodExitEvent, MethodEntryEvent}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.events.filters.MethodNameFilter

class MethodNameFilterProcessorSpec extends test.ParallelMockFunSpec
{
  private val testName = "some name"
  private val methodNameFilter = MethodNameFilter(name = testName)
  private val methodNameProcessor =
    new MethodNameFilterProcessor(methodNameFilter)

  describe("MethodNameFilterProcessor") {
    describe("#process") {
      describe("for MethodEntryEvent") {
        it("should return false if the method name does not match the filter") {
          val expected = false

          val mockEvent = mock[MethodEntryEvent]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning(testName + 1).once()
          }

          val actual = methodNameProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the method name does match the filter") {
          val expected = true

          val mockEvent = mock[MethodEntryEvent]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning(testName).once()
          }

          val actual = methodNameProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for MethodExitEvent") {
        it("should return false if the method name does not match the filter") {
          val expected = false

          val mockEvent = mock[MethodExitEvent]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning(testName + 1).once()
          }

          val actual = methodNameProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the method name does match the filter") {
          val expected = true

          val mockEvent = mock[MethodExitEvent]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning(testName).once()
          }

          val actual = methodNameProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for LocatableEvent") {
        it("should return false if the method name does not match the filter") {
          val expected = false

          val mockEvent = mock[LocatableEvent]
          val mockLocation = mock[Location]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.location _).expects().returning(mockLocation).once()
            (mockLocation.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning(testName + 1).once()
          }

          val actual = methodNameProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the method name does match the filter") {
          val expected = true

          val mockEvent = mock[LocatableEvent]
          val mockLocation = mock[Location]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.location _).expects().returning(mockLocation).once()
            (mockLocation.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning(testName).once()
          }

          val actual = methodNameProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for all other events") {
        it("should always return true") {
          val expected = true

          val mockEvent = mock[Event]

          val actual = methodNameProcessor.process(mockEvent)
          actual should be (expected)
        }
      }
    }
  }
}
