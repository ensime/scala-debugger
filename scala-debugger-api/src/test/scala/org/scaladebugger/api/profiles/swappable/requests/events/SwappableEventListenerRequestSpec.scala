package org.scaladebugger.api.profiles.swappable.requests.events

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.EventType.EventType
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.swappable.SwappableDebugProfile
import org.scaladebugger.api.profiles.traits.DebugProfile

class SwappableEventListenerRequestSpec extends test.ParallelMockFunSpec
{
  private val mockDebugProfile = mock[DebugProfile]
  private val mockProfileManager = mock[ProfileManager]

  private val swappableDebugProfile = new Object with SwappableDebugProfile {
    override protected val profileManager: ProfileManager = mockProfileManager
  }

  describe("SwappableEventListenerRequest") {
    describe("#eventHandlers") {
      it("should invoke the method on the underlying profile") {
        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.eventHandlers _).expects().once()

        swappableDebugProfile.eventHandlers
      }

      it("should throw an exception if there is no underlying profile") {
        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.eventHandlers
        }
      }
    }

    describe("#tryCreateEventListenerWithData") {
      // TODO: ScalaMock is causing a stack overflow exception
      ignore("should invoke the method on the underlying profile") {
        val eventType = mock[EventType]
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*)
          .returning(Some(mockDebugProfile)).once()

        (mockDebugProfile.tryCreateEventListenerWithData _)
          .expects(eventType, arguments).once()

        swappableDebugProfile.tryCreateEventListenerWithData(eventType, arguments: _*)
      }

      it("should throw an exception if there is no underlying profile") {
        val eventType = mock[EventType]
        val arguments = Seq(mock[JDIArgument])

        (mockProfileManager.retrieve _).expects(*).returning(None).once()

        intercept[AssertionError] {
          swappableDebugProfile.tryCreateEventListenerWithData(eventType, arguments: _*)
        }
      }
    }
  }
}
