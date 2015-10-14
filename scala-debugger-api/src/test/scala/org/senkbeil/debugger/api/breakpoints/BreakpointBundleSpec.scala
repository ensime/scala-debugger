package org.senkbeil.debugger.api.breakpoints

import com.sun.jdi.request.BreakpointRequest
import com.sun.jdi.{ObjectReference, ThreadReference, VirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers, OneInstancePerTest}

class BreakpointBundleSpec extends FunSpec with Matchers with BeforeAndAfter
  with MockFactory with OneInstancePerTest
{
  val breakpointRequests = Seq(mock[BreakpointRequest], mock[BreakpointRequest])
  val breakpointBundle = new BreakpointBundle(breakpointRequests)

  describe("BreakpointBundle") {
    describe("#apply") {
      it("should return the breakpoint request at the specific index") {
        val expected = breakpointRequests.head
        val actual = breakpointBundle(0)

        actual should be (expected)
      }

      it("should throw an exception if the index is invalid") {
        val totalRequests = breakpointRequests.length

        // Access 1 past max range
        intercept[IndexOutOfBoundsException] {
          breakpointBundle(totalRequests)
        }
      }
    }

    describe("#length") {
      it("should return the total number of breakpoint requests contained") {
        val expected = breakpointRequests.length
        val actual = breakpointBundle.length

        actual should be (expected)
      }
    }

    describe("#iterator") {
      it("should return an iterator over the contained breakpoint requests") {
        val iterator = breakpointBundle.iterator

        while (iterator.hasNext)
          breakpointRequests should contain (iterator.next())
      }
    }

    describe("#addInstanceFilter") {
      it("should add the instance filter to each underlying breakpoint request") {
        val instance = stub[ObjectReference]

        breakpointRequests.foreach(request =>
          (request.addInstanceFilter _).expects(instance))

        breakpointBundle.addInstanceFilter(instance)
      }
    }

    describe("#addThreadFilter") {
      it("should add the thread filter to each underlying breakpoint request") {
        val threadReference = stub[ThreadReference]

        breakpointRequests.foreach(request =>
          (request.addThreadFilter _).expects(threadReference))

        breakpointBundle.addThreadFilter(threadReference)
      }
    }

    describe("#addCountFilter") {
      it("should add the count filter to each underlying breakpoint request") {
        val count = 3

        breakpointRequests.foreach(request =>
          (request.addCountFilter _).expects(count))

        breakpointBundle.addCountFilter(count)
      }
    }

    describe("#disable") {
      it("should disable each underlying breakpoint request") {
        breakpointRequests.foreach(request =>
          (request.disable _).expects())

        breakpointBundle.disable()
      }
    }

    describe("#enable") {
      it("should enable each underlying breakpoint request") {
        breakpointRequests.foreach(request =>
          (request.enable _).expects())

        breakpointBundle.enable()
      }
    }

    describe("#isEnabled") {
      it("should return true if all breakpoint requests are enabled") {
        val expected = true
        breakpointRequests.foreach(request =>
          (request.isEnabled _).expects().returns(expected))

        val actual = breakpointBundle.isEnabled

        actual should be (expected)
      }

      it("should return false if all breakpoint requests are disabled") {
        val expected = false
        breakpointRequests.foreach(request =>
          (request.isEnabled _).expects().returns(expected))

        val actual = breakpointBundle.isEnabled

        actual should be (expected)
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        val indexedBreakpointRequests = breakpointRequests.zipWithIndex

        indexedBreakpointRequests.filter(_._2 % 2 == 0).map(_._1)
          .foreach(request => (request.isEnabled _).expects().returns(false))

        indexedBreakpointRequests.filter(_._2 % 2 != 0).map(_._1)
          .foreach(request => (request.isEnabled _).expects().returns(true))

        // Out of sync, so should throw an exception
        intercept[AssertionError] {
          breakpointBundle.isEnabled
        }
      }
    }

    describe("#setEnabled") {
      it("should set the enable status of each underlying breakpoint request") {
        val expected = true
        breakpointRequests.foreach(request =>
          (request.setEnabled _).expects(expected))

        breakpointBundle.setEnabled(expected)
      }
    }

    describe("#getProperty") {
      it("should return the property contained by all breakpoint requests") {
        val (key, value) = (mock[AnyRef], mock[AnyRef])
        val expected = value

        breakpointRequests.foreach(request =>
          (request.getProperty _).expects(key).returns(value))

        val actual = breakpointBundle.getProperty(key)

        actual should be (expected)
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        val (key, value1, value2) = (mock[AnyRef], mock[AnyRef], mock[AnyRef])
        val indexedBreakpointRequests = breakpointRequests.zipWithIndex

        indexedBreakpointRequests.filter(_._2 % 2 == 0).map(_._1).foreach(
          request => (request.getProperty _).expects(key).returns(value1))

        indexedBreakpointRequests.filter(_._2 % 2 != 0).map(_._1).foreach(
          request => (request.getProperty _).expects(key).returns(value2))

        // Out of sync, so should throw an exception
        intercept[AssertionError] {
          breakpointBundle.getProperty(key)
        }
      }
    }

    describe("#putProperty") {
      it("should set the property for each underlying breakpoint request") {
        val (key, value) = (mock[AnyRef], mock[AnyRef])
        breakpointRequests.foreach(request =>
          (request.putProperty _).expects(key, value))

        breakpointBundle.putProperty(key, value)
      }
    }

    describe("#setSuspendPolicy") {
      it("should set the suspend policy for each underlying breakpoint request") {
        val policy = 999
        breakpointRequests.foreach(request =>
          (request.setSuspendPolicy _).expects(policy))

        breakpointBundle.setSuspendPolicy(policy)
      }
    }

    describe("#suspendPolicy") {
      it("should return the suspend policy matching all of the underlying breakpoint requests") {
        val expected = 3

        breakpointRequests.foreach(request =>
          (request.suspendPolicy _).expects().returns(expected))

        val actual = breakpointBundle.suspendPolicy

        actual should be (expected)
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        val indexedBreakpointRequests = breakpointRequests.zipWithIndex
        val (suspendPolicy1, suspendPolicy2) = (0, 1)

        indexedBreakpointRequests.filter(_._2 % 2 == 0)
          .map(_._1).foreach(request =>
            (request.suspendPolicy _).expects().returns(suspendPolicy1)
          )

        indexedBreakpointRequests.filter(_._2 % 2 != 0)
          .map(_._1).foreach(request =>
            (request.suspendPolicy _).expects().returns(suspendPolicy2)
          )

        // Out of sync, so should throw an exception
        intercept[AssertionError] {
          breakpointBundle.suspendPolicy
        }
      }
    }

    describe("#virtualMachine") {
      it("should return the virtual machine where all of the underlying breakpoint requests reside") {
        val expected = mock[VirtualMachine]

        breakpointRequests.foreach(request =>
          (request.virtualMachine _).expects().returns(expected))

        val actual = breakpointBundle.virtualMachine

        actual should be (expected)
      }

      it("should throw an exception if the breakpoint requests are out of sync") {
        val indexedBreakpointRequests = breakpointRequests.zipWithIndex
        val (virtualMachine1, virtualMachine2) =
          (mock[VirtualMachine], mock[VirtualMachine])

        indexedBreakpointRequests.filter(_._2 % 2 == 0)
          .map(_._1).foreach(request =>
            (request.virtualMachine _).expects().returns(virtualMachine1)
          )

        indexedBreakpointRequests.filter(_._2 % 2 != 0)
          .map(_._1).foreach(request =>
            (request.virtualMachine _).expects().returns(virtualMachine2)
          )

        // Out of sync, so should throw an exception
        intercept[AssertionError] {
          breakpointBundle.virtualMachine
        }
      }
    }
  }
}
