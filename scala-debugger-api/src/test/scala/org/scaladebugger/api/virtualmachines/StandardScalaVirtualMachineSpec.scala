package org.scaladebugger.api.virtualmachines
import acyclic.file
import com.sun.jdi.VirtualMachine
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.lowlevel.events.EventManager
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.utils.LoopingTaskRunner
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class StandardScalaVirtualMachineSpec extends test.ParallelMockFunSpec
{
  private val mockIsInitialized = mockFunction[Boolean]
  private val mockProcessOwnPendingRequests = mockFunction[Unit]
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockProfileManager = mock[ProfileManager]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]
  private val mockEventManager = mock[EventManager]
  private val scalaVirtualMachine = new StandardScalaVirtualMachine(
    mockVirtualMachine,
    mockProfileManager,
    mockLoopingTaskRunner
  ) {
    override def isInitialized: Boolean = mockIsInitialized()

    override protected def processOwnPendingRequests(): Unit =
      mockProcessOwnPendingRequests()

    override protected lazy val eventManager: EventManager = mockEventManager
  }

  describe("StandardScalaVirtualMachine") {
    describe("#startProcessingEvents") {
      it("should throw an exception if the scala vm is not initialized") {
        mockIsInitialized.expects().returning(false).once()

        intercept[AssertionError] {
          scalaVirtualMachine.startProcessingEvents()
        }
      }

      it("should do nothing if already started") {
        mockIsInitialized.expects().returning(true).once()
        (mockEventManager.isRunning _).expects().returning(true).once()

        scalaVirtualMachine.startProcessingEvents()
      }

      it("should process pending requests and start the event manager") {
        mockIsInitialized.expects().returning(true).once()
        (mockEventManager.isRunning _).expects().returning(false).once()
        mockProcessOwnPendingRequests.expects().once()
        (mockEventManager.start _).expects().once()

        scalaVirtualMachine.startProcessingEvents()
      }
    }

    describe("#stopProcessingEvents") {
      it("should stop the event manager") {
        (mockEventManager.stop _).expects().once()
        scalaVirtualMachine.stopProcessingEvents()
      }
    }

    describe("#isProcessingEvents") {
      it("should return true if the event manager is running") {
        val expected = true

        (mockEventManager.isRunning _).expects().returning(expected).once()

        val actual = scalaVirtualMachine.isProcessingEvents

        actual should be (expected)
      }

      it("should return false if the event manager is not running") {
        val expected = false

        (mockEventManager.isRunning _).expects().returning(expected).once()

        val actual = scalaVirtualMachine.isProcessingEvents

        actual should be (expected)
      }
    }

    describe("#resume") {
      it("should resume the underlying vm") {
        (mockVirtualMachine.resume _).expects().once()
        scalaVirtualMachine.resume()
      }
    }

    describe("#suspend") {
      it("should suspend the underlying vm") {
        (mockVirtualMachine.suspend _).expects().once()
        scalaVirtualMachine.suspend()
      }
    }

    describe("#cache") {
      it("should return the same cache instance") {
        val expected = scalaVirtualMachine.cache

        expected should not be (null)

        val actual = scalaVirtualMachine.cache

        actual should be(expected)
      }
    }

    describe("#register") {
      it("should invoke the underlying profile manager") {
        val testName = "some name"
        val testProfile = mock[DebugProfile]
        val expected = Some(testProfile)

        (mockProfileManager.register _).expects(testName, testProfile)
          .returning(expected).once()

        val actual = scalaVirtualMachine.register(testName, testProfile)
        actual should be (expected)
      }
    }

    describe("#unregister") {
      it("should invoke the underlying profile manager") {
        val testName = "some name"
        val testProfile = mock[DebugProfile]
        val expected = Some(testProfile)

        (mockProfileManager.unregister _).expects(testName)
          .returning(expected).once()

        val actual = scalaVirtualMachine.unregister(testName)
        actual should be (expected)
      }
    }

    describe("#retrieve") {
      it("should invoke the underlying profile manager") {
        val testName = "some name"
        val testProfile = mock[DebugProfile]
        val expected = Some(testProfile)

        (mockProfileManager.retrieve _).expects(testName)
          .returning(expected).once()

        val actual = scalaVirtualMachine.retrieve(testName)
        actual should be (expected)
      }
    }
  }
}
