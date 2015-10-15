package org.senkbeil.debugger.api

import com.sun.jdi.VirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}
import org.senkbeil.debugger.api.jdi.JDILoader

class DebuggerSpec extends FunSpec with Matchers with OneInstancePerTest
  with MockFactory
{
  private class TestDebugger(override val jdiLoader: JDILoader)
    extends Debugger
  {
    override def assertJdiLoaded(): Unit = super.assertJdiLoaded()
    override def start[T](f: (VirtualMachine) => T): Unit = ???
    override def stop(): Unit = ???
    override def isRunning: Boolean = ???
  }

  describe("Debugger") {
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
  }
}
