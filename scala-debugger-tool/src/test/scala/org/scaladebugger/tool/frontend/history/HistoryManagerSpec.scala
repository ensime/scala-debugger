package org.scaladebugger.tool.frontend.history

class HistoryManagerSpec extends test.ParallelMockFunSpec {
  private val mockLines = mockFunction[Seq[String]]
  private val mockWriteLine = mockFunction[String, Unit]
  private val historyManager = new HistoryManager {
    override def maxLines: Int = -1
    override def destroy(): Unit = {}
    override def lines: Seq[String] = mockLines()
    override def writeLine(line: String): Unit = mockWriteLine(line)
  }

  describe("HistoryManager") {
    describe("#size") {
      it("should return zero if the manager's history is empty") {
        val expected = 0

        mockLines.expects().returning((1 to expected).map(_.toString)).once()

        val actual = historyManager.size

        actual should be (expected)
      }

      it("should return the number of lines held by the manager") {
        val expected = 3

        mockLines.expects().returning((1 to expected).map(_.toString)).once()

        val actual = historyManager.size

        actual should be (expected)
      }
    }

    describe("#writeLines") {
      it("should write each line individually") {
        val lines = Seq("one", "two", "three")

        lines.foreach(l => mockWriteLine.expects(l).once())

        historyManager.writeLines(lines: _*)
      }
    }

    describe("#linesByMostRecent") {
      it("should return the reverse order of lines") {
        val expected = Seq("three", "two", "one")

        mockLines.expects().returning(expected.reverse).once()

        val actual = historyManager.linesByMostRecent

        actual should be (expected)
      }
    }
  }
}
