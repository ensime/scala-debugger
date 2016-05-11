package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestThreadStatusInfoProfile

class ThreadStatusInfoProfileSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  private val StateMonitor = 0
  private val StateUnknown = 1
  private val StateSuspended = 2
  private val StateWait = 3
  private val StateSleeping = 4
  private val StateAtBreakpoint = 5
  private val StateNotStarted = 6
  private val StateRunning = 7
  private val StateZombie = 8

  import scala.language.reflectiveCalls
  private val threadStatusInfoProfile = new TestThreadStatusInfoProfile {
    def setStateTo(state: Int): Unit = _state = state
    private var _state: Int = -1
    override def statusCode: Int = _state

    override def isMonitor: Boolean = _state == StateMonitor
    override def isUnknown: Boolean = _state == StateUnknown
    override def isSuspended: Boolean = _state == StateSuspended
    override def isWait: Boolean = _state == StateWait
    override def isSleeping: Boolean = _state == StateSleeping
    override def isAtBreakpoint: Boolean = _state == StateAtBreakpoint
    override def isNotStarted: Boolean = _state == StateNotStarted
    override def isRunning: Boolean = _state == StateRunning
    override def isZombie: Boolean = _state == StateZombie
  }

  describe("ThreadStatusInfoProfile") {
    describe("#statusString") {
      it("should be able to return a string representing the monitor state") {
        val expected = "Monitoring"

        threadStatusInfoProfile.setStateTo(StateMonitor)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the not started state") {
        val expected = "Not Started"

        threadStatusInfoProfile.setStateTo(StateNotStarted)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the unknown state") {
        val expected = "Unknown"

        threadStatusInfoProfile.setStateTo(StateUnknown)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the running state") {
        val expected = "Running"

        threadStatusInfoProfile.setStateTo(StateRunning)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the sleeping state") {
        val expected = "Sleeping"

        threadStatusInfoProfile.setStateTo(StateSleeping)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the wait state") {
        val expected = "Waiting"

        threadStatusInfoProfile.setStateTo(StateWait)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the zombie state") {
        val expected = "Zombie"

        threadStatusInfoProfile.setStateTo(StateZombie)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the breakpoint state") {
        val expected = "Suspended at Breakpoint"

        threadStatusInfoProfile.setStateTo(StateAtBreakpoint)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing the suspended state") {
        val expected = "Suspended"

        threadStatusInfoProfile.setStateTo(StateSuspended)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }

      it("should be able to return a string representing an invalid state") {
        val expected = "Invalid Status Id -1"

        threadStatusInfoProfile.setStateTo(-1)

        val actual = threadStatusInfoProfile.statusString

        actual should be (expected)
      }
    }
  }
}
