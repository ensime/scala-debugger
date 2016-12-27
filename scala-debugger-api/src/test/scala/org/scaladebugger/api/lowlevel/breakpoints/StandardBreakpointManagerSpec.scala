package org.scaladebugger.api.lowlevel.breakpoints

import com.sun.jdi.{Location, VirtualMachine}
import com.sun.jdi.request.{BreakpointRequest, EventRequest, EventRequestManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.classes.ClassManager
import org.scaladebugger.api.lowlevel.requests.{JDIRequestArgument, JDIRequestArgumentProcessor, JDIRequestProcessor}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.JDIMockHelpers

import scala.util.{Failure, Success}

class StandardBreakpointManagerSpec extends ParallelMockFunSpec with JDIMockHelpers
{
  private val TestRequestId = java.util.UUID.randomUUID().toString
  private val mockEventRequestManager = mock[EventRequestManager]
  private val stubVirtualMachine = stub[VirtualMachine]
  private val mockClassManager = mock[ClassManager]

  private val breakpointManager = new StandardBreakpointManager(
    mockEventRequestManager,
    mockClassManager
  ) {
    override protected def newRequestId(): String = TestRequestId
  }

  describe("StandardBreakpointManager") {
    describe("#breakpointRequestListById") {
      it("should return a collection of breakpoint file names and lines") {
        val expected = Seq(
          ("id1", "file1", 1),
          ("id2", "file1", 2),
          ("id3", "file2", 999)
        )

        // Build the map to return from linesAndLocationsForFile(...)
        (mockClassManager.linesAndLocationsForFile _).expects(*).onCall(
          (fileName: String) => Some(expected
            .map(t => (t._2, t._3))
            .groupBy(_._1)(fileName)
            .map(_._2)
            .map(i => (i, Seq(createRandomLocationStub())))
            .toMap)
        ).repeated(expected.length).times()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).repeated(expected.length).times()

        expected.foreach(b =>
          breakpointManager.createBreakpointRequestWithId(b._1, b._2, b._3)
        )

        val actual = breakpointManager.breakpointRequestListById

        actual should contain theSameElementsAs expected.map(_._1)
      }

      it("should return an empty collection if no breakpoints have been set") {
        breakpointManager.breakpointRequestListById should be (empty)
      }
    }

    describe("#breakpointRequestList") {
      it("should return a collection of breakpoint file names and lines") {
        val mockRequestArgument = mock[JDIRequestArgument]
        val expected = Seq(
          BreakpointRequestInfo(TestRequestId, false, "file1", 1),
          BreakpointRequestInfo(TestRequestId + 1, false, "file1", 2, Seq(mockRequestArgument)),
          BreakpointRequestInfo(TestRequestId + 2, false, "file2", 999)
        )

        // NOTE: Must create a new breakpoint manager that does NOT override the
        //       request id to always be the same since we do not allow
        //       duplicates of the test id when storing it
        val breakpointManager = new StandardBreakpointManager(
          mockEventRequestManager,
          mockClassManager
        )

        // Build the map to return from linesAndLocationsForFile(...)
        (mockClassManager.linesAndLocationsForFile _).expects(*).onCall(
          (fileName: String) => Some(expected
            .groupBy(_.fileName)(fileName)
            .map(_.lineNumber)
            .map(i => (i, Seq(createRandomLocationStub())))
            .toMap)
        ).repeated(expected.length).times()

        val mockRequestProcessor = mock[JDIRequestProcessor]
        (mockRequestArgument.toProcessor _).expects()
          .returning(mockRequestProcessor).once()

        (mockRequestProcessor.process _).expects(*)
          .onCall((er: EventRequest) => er).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).repeated(expected.length).times()

        expected.foreach(b => breakpointManager.createBreakpointRequestWithId(
          b.requestId, b.fileName, b.lineNumber, b.extraArguments: _*
        ))

        val actual = breakpointManager.breakpointRequestList

        actual should contain theSameElementsAs expected
      }

      it("should return an empty collection if no breakpoints have been set") {
        breakpointManager.breakpointRequestList should be (empty)
      }
    }

    describe("#createBreakpointRequestWithId") {
      it("should create a new breakpoint request using the provided id") {
        val expected = Success(java.util.UUID.randomUUID().toString)
        val testFileName = "some/file/name"
        val testLineNumber = 1

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        val actual = breakpointManager.createBreakpointRequestWithId(
          expected.get,
          testFileName,
          testLineNumber
        )

        actual should be(expected)
      }

      it("should return NoBreakpointLocationFound if the file is not available") {
        val testFileName = "some/file/name"
        val testLineNumber = 999
        val expected = Failure(NoBreakpointLocationFound(
          testFileName, testLineNumber
        ))

        // Mark the retrieval of lines and locations to indicate no file
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(None)

        val actual = breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return NoBreakpointLocationFound if the line is not available") {
        val testFileName = "some/file/name"
        val testLineNumber = 999
        val expected = Failure(NoBreakpointLocationFound(
          testFileName, testLineNumber
        ))

        // Mark the retrieval of lines and locations to a map with
        // a line number that will NOT be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> (Nil: Seq[Location]))))

        // Set a breakpoint on a line that is NOT returned by linesAndLocations
        val actual = breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          testFileName,
          testLineNumber
        )

        actual should be (expected)
      }

      it("should return Success(id) if successfully added the breakpoint") {
        val expected = Success(TestRequestId)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        // Set a breakpoint on a line that is returned by linesAndLocations
        val actual = breakpointManager.createBreakpointRequestWithId(
          expected.get,
          "",
          1
        )

        actual should be (expected)
      }

      it("should create a new breakpoint request for each matching location") {
        val locationsPerLine = 3

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked and one that will not
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map(
            0 -> (1 to locationsPerLine).map(_ => createRandomLocationStub()),
            1 -> (1 to locationsPerLine).map(_ => createRandomLocationStub())
          ))
        )

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(locationsPerLine).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequestWithId(TestRequestId, "", 1)
      }

      it("should return the failure if unable to create one of the underlying breakpoint requests") {
        val expected = Failure(new Throwable)
        val locationsPerLine = 3

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked and one that will not
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map(
            0 -> (1 to locationsPerLine).map(_ => createRandomLocationStub()),
            1 -> (1 to locationsPerLine).map(_ => createRandomLocationStub())
          ))
        )

        // Stub out the call to create a breakpoint request (except last)
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(locationsPerLine - 1).times()

        // TODO: Does this actually throw an exception?
        // Stub out the call to create last breakpoint request to
        // throw an exception
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .throwing(expected.failed.get).once()

        // Set a breakpoint on a line that is returned by linesAndLocations
        val actual = breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          "",
          1
        )

        actual should be (expected)
      }
    }

    describe("#hasBreakpointRequestWithId") {
      it("should return true if a breakpoint request exists with the id") {
        val expected = true

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Verify that we have the file and line in our list
        val actual = breakpointManager.hasBreakpointRequestWithId(TestRequestId)

        actual should be (expected)
      }

      it("should return true if a breakpoint request does not exist with the id") {
        val expected = false

        val actual = breakpointManager.hasBreakpointRequestWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#hasBreakpointRequest") {
      it("should return true if the breakpoint with matching file name and line is found") {
        val expected = true

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequest("file", 1)

        // Verify that we have the file and line in our list
        val actual = breakpointManager.hasBreakpointRequest("file", 1)

        actual should be (expected)
      }

      it("should return false if no breakpoint is found") {
        val expected = false

        val actual = breakpointManager.hasBreakpointRequest("file", 1)

        actual should be (expected)
      }
    }

    describe("#getBreakpointRequestWithId") {
      it("should return Some(breakpoint requests) if the id matches something") {
        val stubBreakpointRequest = stub[BreakpointRequest]
        val expected = Seq(stubBreakpointRequest)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stubBreakpointRequest)

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Should not be empty
        val actual = breakpointManager
          .getBreakpointRequestWithId(TestRequestId)
          .get

        actual should be (expected)
      }

      it("should return None if no breakpoint with the id is found") {
        breakpointManager.getBreakpointRequestWithId(TestRequestId) should
          be (None)
      }
    }

    describe("#getBreakpointRequestInfoWithId") {
      it("should return Some(BreakpointInfo(id, not pending, class name, line number)) if the id exists") {
        val expected = Some(BreakpointRequestInfo(TestRequestId, false, "file", 1))

        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(
          Some(Map(expected.get.lineNumber -> Seq(createRandomLocationStub())))
        ).once()

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest]).once()

        breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          expected.get.fileName,
          expected.get.lineNumber
        )

        val actual = breakpointManager.getBreakpointRequestInfoWithId(TestRequestId)

        actual should be (expected)
      }

      it("should return None if there is no breakpoint with the id") {
        val expected = None

        val actual = breakpointManager.getBreakpointRequestInfoWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#getBreakpointRequest") {
      it("should return Some(collection of breakpoints representing the line)") {
        val stubBreakpointRequest = stub[BreakpointRequest]
        val expected = Seq(stubBreakpointRequest)

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> Seq(createRandomLocationStub()))))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stubBreakpointRequest)

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequest("file", 1)

        // Should not be empty
        val actual = breakpointManager
          .getBreakpointRequest("file", 1)
          .get

        actual should be (expected)
      }

      it("should return None if no breakpoint is found") {
        breakpointManager.getBreakpointRequest("file", 1) should be (None)
      }
    }

    describe("#removeBreakpointRequestWithId") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true
        val totalBreakpointRequests = 3

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations)))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        val actual =
          breakpointManager.removeBreakpointRequestWithId(TestRequestId)

        actual should be (expected)
      }

      it("should delete each breakpoint request represented by the bundle") {
        val totalBreakpointRequests = 3

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations)))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequestWithId(
          TestRequestId,
          "file",
          1
        )

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        breakpointManager.removeBreakpointRequestWithId(TestRequestId)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false
        val actual =
          breakpointManager.removeBreakpointRequestWithId(TestRequestId)

        actual should be (expected)
      }
    }

    describe("#removeBreakpointRequest") {
      it("should return true if the breakpoint was successfully deleted") {
        val expected = true
        val totalBreakpointRequests = 3

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations)))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequest("file", 1)

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        val actual = breakpointManager.removeBreakpointRequest("file", 1)

        actual should be (expected)
      }

      it("should delete each breakpoint request represented by the bundle") {
        val totalBreakpointRequests = 3

        // Create X locations that will result in X breakpoint requests
        val locations = (1 to totalBreakpointRequests)
          .map(_ => createRandomLocationStub())

        // Mark the retrieval of lines and locations to a map with
        // a line number that will be the one picked
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(Map(1 -> locations)))

        // Stub out the call to create a breakpoint request
        (mockEventRequestManager.createBreakpointRequest _).expects(*)
          .returning(stub[BreakpointRequest])
          .repeated(totalBreakpointRequests).times()

        // Set a breakpoint on a line that is returned by linesAndLocations
        breakpointManager.createBreakpointRequest("file", 1)

        // Should remove X breakpoint requests through one call
        (mockEventRequestManager.deleteEventRequests _).expects(where {
          l: java.util.List[_ <: EventRequest] =>
            l.size == totalBreakpointRequests
        }).once()

        breakpointManager.removeBreakpointRequest("file", 1)
      }

      it("should return false if the breakpoint was not found") {
        val expected = false
        val actual = breakpointManager.removeBreakpointRequest("file", 1)

        actual should be (expected)
      }
    }
  }
}
