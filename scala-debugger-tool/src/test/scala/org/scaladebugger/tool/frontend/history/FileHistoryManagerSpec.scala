package org.scaladebugger.tool.frontend.history

import java.io.{File, PrintWriter, Writer}

class FileHistoryManagerSpec extends test.ParallelMockFunSpec {
  private class TestFile extends File("")
  private val stubFile = stub[TestFile]

  private val stubWriter = stub[Writer]
  private abstract class TestPrintWriter extends PrintWriter(stubWriter)
  private val stubPrintWriter = stub[TestPrintWriter]

  describe("FileHistoryManager") {
    describe("#newInstance") {
      it("should set the maximum lines to the provided value") {
        val expected = 999

        val fhm = FileHistoryManager.newInstance(
          f = stubFile,
          maxLines = expected,
          newPrintWriter = _ => stubPrintWriter
        )

        val actual = fhm.maxLines

        actual should be (expected)
      }

      it("should pre-populate the memory with the lines from the file") {
        val expected = Seq("one", "two", "three")

        val mockLoadLines = mockFunction[File, Seq[String]]
        mockLoadLines.expects(stubFile).returning(expected).once()

        val fhm = FileHistoryManager.newInstance(
          f = stubFile,
          loadLines = mockLoadLines,
          newPrintWriter = _ => stubPrintWriter
        )

        val actual = fhm.lines

        actual.reverse should be (expected)
      }
    }

    describe("#writeLine") {
      it("should write the line to internal memory and the history file") {
        val initialLines = Seq("one", "two", "three")
        val newLine = "four"
        val expected = (initialLines :+ newLine).reverse

        val mockLoadLines = mockFunction[File, Seq[String]]
        mockLoadLines.expects(stubFile).returning(initialLines).once()

        val mockPrintWriter = mock[TestPrintWriter]
        (mockPrintWriter.println(_: String)).expects(newLine).once()

        val fhm = FileHistoryManager.newInstance(
          f = stubFile,
          loadLines = mockLoadLines,
          newPrintWriter = _ => mockPrintWriter
        )

        fhm.writeLine(newLine)

        val actual = fhm.lines

        actual should be (expected)
      }

      it("should do nothing if maxLines == 0") {
        val maxLines = 0
        val initialLines = Seq("one", "two", "three")
        val newLine = "four"
        val expected = initialLines.reverse

        val mockLoadLines = mockFunction[File, Seq[String]]
        mockLoadLines.expects(stubFile).returning(initialLines).once()

        val mockPrintWriter = mock[TestPrintWriter]
        (mockPrintWriter.println(_: String)).expects(*).never()

        val fhm = FileHistoryManager.newInstance(
          f = stubFile,
          maxLines = maxLines,
          loadLines = mockLoadLines,
          newPrintWriter = _ => mockPrintWriter
        )

        fhm.writeLine(newLine)

        val actual = fhm.lines

        actual should be (expected)
      }

      it("should remove the oldest line from internal memory and history file when reached maxLines") {
        val maxLines = 3
        val initialLines = Seq("one", "two", "three")
        val newLine = "four"
        val expected = (initialLines :+ newLine).takeRight(maxLines).reverse

        val ls = System.getProperty("line.separator")

        val mockLoadLines = mockFunction[File, Seq[String]]
        mockLoadLines.expects(stubFile).returning(initialLines).once()

        val mockPrintWriter = mock[TestPrintWriter]
        (mockPrintWriter.close _).expects().once()
        expected.foreach(l => {
          (mockPrintWriter.write(_: String)).expects(l + ls).once()
        })
        (mockPrintWriter.flush _).expects().once()

        val fhm = FileHistoryManager.newInstance(
          f = stubFile,
          maxLines = maxLines,
          loadLines = mockLoadLines,
          newPrintWriter = _ => mockPrintWriter
        )

        fhm.writeLine(newLine)

        val actual = fhm.lines

        actual should be (expected)
      }
    }

    describe("#destroy") {
      it("should clear the history held in memory and the history file") {
        // Closes existing print writer
        val mockPrintWriter = mock[TestPrintWriter]
        (mockPrintWriter.close _).expects().once()

        // Deletes the file
        val mockFile = mock[TestFile]
        (mockFile.delete _).expects().returning(true).once()

        // Creates a new print writer on init and after
        // destroying the old one
        val mockNewPrintWriter = mockFunction[File, PrintWriter]
        mockNewPrintWriter.expects(mockFile).returning(mockPrintWriter).twice()

        val fhm = FileHistoryManager.newInstance(
          f = mockFile,
          newPrintWriter = mockNewPrintWriter
        )

        fhm.writeLines("one", "two", "three")

        fhm.destroy()

        fhm.lines should be (empty)
      }
    }
  }
}
