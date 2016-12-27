package org.scaladebugger.api.lowlevel.exceptions

import java.util.concurrent.atomic.AtomicBoolean

import com.sun.jdi.event.ExceptionEvent
import org.scaladebugger.api.lowlevel.events.EventType._
import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.api.virtualmachines.DummyScalaVirtualMachine
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

class StandardExceptionManagerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("StandardExceptionManager") {
    it("should be able to detect exceptions in try blocks") {
      val testClass =
        "org.scaladebugger.test.exceptions.InsideTryBlockException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.scaladebugger.test.exceptions.CustomException"

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark the exception we want to watch (now that the class
      // is available)
      exceptionManager.createExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = true,
        notifyUncaught = false
      )

      eventManager.addResumingEventHandler(ExceptionEventType, e => {
        val exceptionEvent = e.asInstanceOf[ExceptionEvent]
        val exceptionName = exceptionEvent.exception().referenceType().name()

        logger.debug(s"Detected exception: $exceptionName")
        if (exceptionName == expectedExceptionName)
          detectedException.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions in functional try calls") {
      val testClass =
        "org.scaladebugger.test.exceptions.InsideFunctionalTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.scaladebugger.test.exceptions.CustomException"

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark the exception we want to watch (now that the class
      // is available)
      exceptionManager.createExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = true,
        notifyUncaught = false
      )

      eventManager.addResumingEventHandler(ExceptionEventType, e => {
        val exceptionEvent = e.asInstanceOf[ExceptionEvent]
        val exceptionName = exceptionEvent.exception().referenceType().name()

        logger.debug(s"Detected exception: $exceptionName")
        if (exceptionName == expectedExceptionName)
          detectedException.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }

    it("should be able to detect exceptions outside of try blocks") {
      val testClass =
        "org.scaladebugger.test.exceptions.OutsideTryException"
      val testFile = JDITools.scalaClassStringToFileString(testClass)

      val detectedException = new AtomicBoolean(false)
      val expectedExceptionName =
        "org.scaladebugger.test.exceptions.CustomException"

      val s = DummyScalaVirtualMachine.newInstance()
      import s.lowlevel._

      // Mark the exception we want to watch (now that the class
      // is available)
      exceptionManager.createExceptionRequest(
        exceptionName = expectedExceptionName,
        notifyCaught = false,
        notifyUncaught = true
      )

      eventManager.addResumingEventHandler(ExceptionEventType, e => {
        val exceptionEvent = e.asInstanceOf[ExceptionEvent]
        val exceptionName = exceptionEvent.exception().referenceType().name()

        logger.debug(s"Detected exception: $exceptionName")
        if (exceptionName == expectedExceptionName)
          detectedException.set(true)
      })

      withVirtualMachine(testClass, pendingScalaVirtualMachines = Seq(s)) { (s) =>
        logTimeTaken(eventually {
          detectedException.get() should be (true)
        })
      }
    }
  }
}
