package org.scaladebugger.api.lowlevel.events.filters.processors

import com.sun.jdi.event._
import com.sun.jdi._
import org.scaladebugger.api.lowlevel.events.filters.WildcardPatternFilter
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class WildcardPatternFilterProcessorSpec extends ParallelMockFunSpec
{
  private val testPattern = "some*pattern"
  private val wildcardPatternFilter = WildcardPatternFilter(
    pattern = testPattern
  )
  private val wildcardPatternProcessor =
    new WildcardPatternFilterProcessor(wildcardPatternFilter)

  describe("WildcardPatternFilterProcessor") {
    describe("#process") {
      describe("for ClassPrepareEvent") {
        it("should return false if the class name does not match the filter") {
          val expected = false

          val mockEvent = mock[ClassPrepareEvent]
          val mockReferenceType = mock[ReferenceType]

          inSequence {
            (mockEvent.referenceType _).expects().returning(mockReferenceType).once()
            (mockReferenceType.name _).expects().returning("not.some.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the class name does match the filter") {
          val expected = true

          val mockEvent = mock[ClassPrepareEvent]
          val mockReferenceType = mock[ReferenceType]

          inSequence {
            (mockEvent.referenceType _).expects().returning(mockReferenceType).once()
            (mockReferenceType.name _).expects().returning("some.Class.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for ClassUnloadEvent") {
        it("should return false if the class name does not match the filter") {
          val expected = false

          val mockEvent = mock[ClassUnloadEvent]

          (mockEvent.className _).expects().returning("not.some.pattern").once()

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the class name does match the filter") {
          val expected = true

          val mockEvent = mock[ClassUnloadEvent]

          (mockEvent.className _).expects().returning("some.Class.pattern").once()

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for ExceptionEvent") {
        it("should return false if the class name does not match the filter") {
          val expected = false

          val mockEvent = mock[ExceptionEvent]
          val mockObjectReference = mock[ObjectReference]
          val mockReferenceType = mock[ReferenceType]

          inSequence {
            (mockEvent.exception _).expects().returning(mockObjectReference).once()
            (mockObjectReference.referenceType _).expects().returning(mockReferenceType).once()
            (mockReferenceType.name _).expects().returning("not.some.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the class name does match the filter") {
          val expected = true

          val mockEvent = mock[ExceptionEvent]
          val mockObjectReference = mock[ObjectReference]
          val mockReferenceType = mock[ReferenceType]

          inSequence {
            (mockEvent.exception _).expects().returning(mockObjectReference).once()
            (mockObjectReference.referenceType _).expects().returning(mockReferenceType).once()
            (mockReferenceType.name _).expects().returning("some.Exception.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for MethodEntryEvent") {
        it("should return false if the method name does not match the filter") {
          val expected = false

          val mockEvent = mock[MethodEntryEvent]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning("not some pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the method name does match the filter") {
          val expected = true

          val mockEvent = mock[MethodEntryEvent]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning("someMethodpattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
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
            (mockMethod.name _).expects().returning("not some pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the method name does match the filter") {
          val expected = true

          val mockEvent = mock[MethodExitEvent]
          val mockMethod = mock[Method]

          inSequence {
            (mockEvent.method _).expects().returning(mockMethod).once()
            (mockMethod.name _).expects().returning("someMethodpattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for ThreadDeathEvent") {
        it("should return false if the thread name does not match the filter") {
          val expected = false

          val mockEvent = mock[ThreadDeathEvent]
          val mockThreadReference = mock[ThreadReference]

          inSequence {
            (mockEvent.thread _).expects().returning(mockThreadReference).once()
            (mockThreadReference.name _).expects().returning("not.some.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the thread name does match the filter") {
          val expected = true

          val mockEvent = mock[ThreadDeathEvent]
          val mockThread = mock[ThreadReference]

          inSequence {
            (mockEvent.thread _).expects().returning(mockThread).once()
            (mockThread.name _).expects().returning("some.Thread.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for ThreadStartEvent") {
        it("should return false if the thread name does not match the filter") {
          val expected = false

          val mockEvent = mock[ThreadStartEvent]
          val mockThreadReference = mock[ThreadReference]

          inSequence {
            (mockEvent.thread _).expects().returning(mockThreadReference).once()
            (mockThreadReference.name _).expects().returning("not.some.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }

        it("should return true if the thread name does match the filter") {
          val expected = true

          val mockEvent = mock[ThreadStartEvent]
          val mockThreadReference = mock[ThreadReference]

          inSequence {
            (mockEvent.thread _).expects().returning(mockThreadReference).once()
            (mockThreadReference.name _).expects().returning("some.Thread.pattern").once()
          }

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }
      }

      describe("for all other events") {
        it("should always return true") {
          val expected = true

          val mockEvent = mock[Event]

          val actual = wildcardPatternProcessor.process(mockEvent)
          actual should be (expected)
        }
      }
    }
  }
}
