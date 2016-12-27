package org.scaladebugger.api.debuggers

import org.scaladebugger.api.utils.JDILoader
import org.scaladebugger.api.virtualmachines.{DummyScalaVirtualMachine, ScalaVirtualMachine, ScalaVirtualMachineManager}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class DebuggerSpec extends ParallelMockFunSpec with MockFactory with ScalaFutures
{
  private val mockScalaVirtualMachineManager = mock[ScalaVirtualMachineManager]
  private class TestDebugger(override val jdiLoader: JDILoader)
    extends Debugger
  {
    override def assertJdiLoaded(): Unit = super.assertJdiLoaded()
    override def stop(): Unit = ???
    override def isRunning: Boolean = ???
    override def start[T](
      defaultProfile: String,
      startProcessingEvents: Boolean,
      newVirtualMachineFunc: (ScalaVirtualMachine) => T
    ): Unit = ???
    override def newDummyScalaVirtualMachine(): ScalaVirtualMachine = ???
    override def scalaVirtualMachineManager: ScalaVirtualMachineManager =
      mockScalaVirtualMachineManager
  }

  describe("Debugger") {
    describe("#connectedScalaVirtualMachines") {
      it("should return all non-dummy scala virtual machines in the manager") {
        val expected = Seq(mock[ScalaVirtualMachine], mock[ScalaVirtualMachine])
        val allSVMs = expected :+ new DummyScalaVirtualMachine(null, null)

        (mockScalaVirtualMachineManager.toSVMs _).expects()
          .returning(allSVMs).once()

        val debugger = new TestDebugger(new JDILoader())
        val actual = debugger.connectedScalaVirtualMachines

        actual should be (expected)
      }
    }

    describe("#start") {
      it("should be able to return a future for the first new JVM") {
        var newVMFunc: ScalaVirtualMachine => _ = null

        val debugger = new TestDebugger(new JDILoader()) {
          override def start[T](
            defaultProfile: String,
            startProcessingEvents: Boolean,
            newVirtualMachineFunc: (ScalaVirtualMachine) => T
          ): Unit = newVMFunc = newVirtualMachineFunc
        }

        val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
        val f = debugger.start()
        newVMFunc(mockScalaVirtualMachine)

        whenReady(f) { s => s should be (mockScalaVirtualMachine) }
      }

      it("should be able to return a future that ignores any additional JVMs") {
        var newVMFunc: ScalaVirtualMachine => _ = null

        val debugger = new TestDebugger(new JDILoader()) {
          override def start[T](
            defaultProfile: String,
            startProcessingEvents: Boolean,
            newVirtualMachineFunc: (ScalaVirtualMachine) => T
          ): Unit = newVMFunc = newVirtualMachineFunc
        }

        val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
        val f = debugger.start()

        newVMFunc(mockScalaVirtualMachine)
        newVMFunc(mock[ScalaVirtualMachine])
        newVMFunc(mock[ScalaVirtualMachine])

        whenReady(f) { s => s should be (mockScalaVirtualMachine) }
      }

      it("should be able to wait for a future to complete") {
        val mockScalaVirtualMachine = mock[ScalaVirtualMachine]

        val debugger = new TestDebugger(new JDILoader()) {
          override def start[T](
            defaultProfile: String,
            startProcessingEvents: Boolean,
            newVirtualMachineFunc: (ScalaVirtualMachine) => T
          ): Unit = newVirtualMachineFunc(mockScalaVirtualMachine)
        }

        import scala.concurrent.duration._
        debugger.start(1.second) should be (mockScalaVirtualMachine)
      }

      it("should throw an exception if the returned future fails to complete") {
        import scala.concurrent.duration._
        val timeout = 10.milliseconds

        val debugger = new TestDebugger(new JDILoader()) {
          override def start[T](
            defaultProfile: String,
            startProcessingEvents: Boolean,
            newVirtualMachineFunc: (ScalaVirtualMachine) => T
          ): Unit = {}
        }

        intercept[scala.concurrent.TimeoutException] {
          debugger.start(timeout)
        }
      }
    }

    describe("#isAvailable") {
      it("should return true if jdi loader is available") {
        val expected = true

        // NOTE: Unable to mock default argument
        // See https://github.com/paulbutcher/ScalaMock/issues/43
        val _jdiLoader = new JDILoader() {
          override def isJdiAvailable(classLoader: ClassLoader): Boolean = true
        }
        val debugger = new TestDebugger(_jdiLoader)

        val actual = debugger.isAvailable

        actual should be (expected)
      }

      it("should return false if jdi loader is not available") {
        val expected = false

        // NOTE: Unable to mock default argument
        // See https://github.com/paulbutcher/ScalaMock/issues/43
        val _jdiLoader = new JDILoader() {
          override def isJdiAvailable(classLoader: ClassLoader): Boolean = false
        }
        val debugger = new TestDebugger(_jdiLoader)

        val actual = debugger.isAvailable

        actual should be (expected)
      }
    }

    describe("#assertJdiLoaded") {
      it("should throw an assertion error if failed to load jdi") {
        // NOTE: Unable to mock default argument
        // See https://github.com/paulbutcher/ScalaMock/issues/43
        val _jdiLoader = new JDILoader() {
          override def tryLoadJdi(classLoader: ClassLoader): Boolean = false
        }
        val debugger = new TestDebugger(_jdiLoader)

        intercept[AssertionError] {
          debugger.assertJdiLoaded()
        }
      }

      it("should do nothing if successfully loaded jdi") {
        // NOTE: Unable to mock default argument
        // See https://github.com/paulbutcher/ScalaMock/issues/43
        val _jdiLoader = new JDILoader() {
          override def tryLoadJdi(classLoader: ClassLoader): Boolean = true
        }
        val debugger = new TestDebugger(_jdiLoader)

        debugger.assertJdiLoaded()
      }
    }

    describe("#addPendingScalaVirtualMachine") {
      it("should add the virtual machine to the list") {
        val debugger = new TestDebugger(null)

        val testUniqueId = java.util.UUID.randomUUID().toString
        val scalaVirtualMachine = new DummyScalaVirtualMachine(null, null) {
          override val uniqueId: String = testUniqueId
        }

        val expected = Some(scalaVirtualMachine)

        val actual = debugger.addPendingScalaVirtualMachine(scalaVirtualMachine)

        actual should be (expected)
        debugger.getPendingScalaVirtualMachines should contain (expected.get)
      }

      it("should not add the virtual machine if one with the same id has already been added") {
        val debugger = new TestDebugger(null)

        val testUniqueId = java.util.UUID.randomUUID().toString
        val scalaVirtualMachine = new DummyScalaVirtualMachine(null, null) {
          override val uniqueId: String = testUniqueId
        }

        debugger.addPendingScalaVirtualMachine(scalaVirtualMachine)

        val expected = None

        val actual = debugger.addPendingScalaVirtualMachine(scalaVirtualMachine)

        actual should be (expected)
        debugger.getPendingScalaVirtualMachines should contain (scalaVirtualMachine)
      }
    }

    describe("#removePendingScalaVirtualMachine") {
      it("should remove the virtual machine from the list") {
        val debugger = new TestDebugger(null)

        val testUniqueId = java.util.UUID.randomUUID().toString
        val scalaVirtualMachine = new DummyScalaVirtualMachine(null, null) {
          override val uniqueId: String = testUniqueId
        }

        debugger.addPendingScalaVirtualMachine(scalaVirtualMachine)

        debugger.removePendingScalaVirtualMachine(scalaVirtualMachine.uniqueId)

        debugger.getPendingScalaVirtualMachines should be (empty)
      }

      it("should do nothing if no virtual machine with the id has been added") {
        val debugger = new TestDebugger(null)

        debugger.removePendingScalaVirtualMachine("") should be (None)
      }
    }

    describe("#withPending") {
      it("should add the virtual machine to the list") {
        val debugger = new TestDebugger(null)

        val testUniqueId = java.util.UUID.randomUUID().toString
        val scalaVirtualMachine = new DummyScalaVirtualMachine(null, null) {
          override val uniqueId: String = testUniqueId
        }

        debugger.withPending(scalaVirtualMachine)

        debugger.getPendingScalaVirtualMachines should contain (scalaVirtualMachine)
      }

      it("should not add the virtual machine if one with the same id has already been added") {
        val debugger = new TestDebugger(null)

        val testUniqueId = java.util.UUID.randomUUID().toString
        val scalaVirtualMachine = new DummyScalaVirtualMachine(null, null) {
          override val uniqueId: String = testUniqueId
        }

        val expected = Seq(scalaVirtualMachine)

        debugger
          .withPending(scalaVirtualMachine)
          .withPending(scalaVirtualMachine)

        val actual = debugger.getPendingScalaVirtualMachines

        actual should be (expected)
      }
    }

    describe("#withoutPending") {
      it("should remove the virtual machine from the list") {
        val debugger = new TestDebugger(null)

        val testUniqueId = java.util.UUID.randomUUID().toString
        val scalaVirtualMachine = new DummyScalaVirtualMachine(null, null) {
          override val uniqueId: String = testUniqueId
        }

        debugger
          .withPending(scalaVirtualMachine)
          .withoutPending(scalaVirtualMachine.uniqueId)

        debugger.getPendingScalaVirtualMachines should be (empty)
      }

      it("should do nothing if no virtual machine with the id has been added") {
        val debugger = new TestDebugger(null)

        debugger.withoutPending(java.util.UUID.randomUUID().toString)

        debugger.getPendingScalaVirtualMachines should be (empty)
      }
    }
  }
}
