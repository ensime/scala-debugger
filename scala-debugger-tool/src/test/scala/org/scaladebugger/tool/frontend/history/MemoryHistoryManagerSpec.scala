package org.scaladebugger.tool.frontend.history

import com.sun.jdi.connect.{AttachingConnector, Connector}
import com.sun.jdi.{VirtualMachine, VirtualMachineManager}
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.utils.LoopingTaskRunner
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, StandardScalaVirtualMachine}

import scala.collection.JavaConverters._

class MemoryHistoryManagerSpec extends test.ParallelMockFunSpec {
  describe("MemoryHistoryManager") {
    describe("#newInstance") {
      it("should set the maximum lines to the provided value") {
        val expected = 999

        val mhm = MemoryHistoryManager.newInstance(maxLines = expected)

        val actual = mhm.maxLines

        actual should be (expected)
      }

      it("should pre-populate the memory with the initial lines") {
        val expected = Seq("one", "two", "three")

        val mhm = MemoryHistoryManager.newInstance(initialLines = expected)

        val actual = mhm.lines

        actual should be (expected)
      }
    }

    describe("#writeLine") {
      it("should add the line to the end of the collection of lines") {
        val initialLines = Seq("one", "two", "three")
        val newLine = "four"
        val expected = initialLines :+ newLine

        val mhm = MemoryHistoryManager.newInstance(
          maxLines = -1,
          initialLines = initialLines
        )
        mhm.writeLine(newLine)

        val actual = mhm.lines

        actual should be (expected)
      }

      it("should do nothing if maxLines == 0") {
        val initialLines = Seq("one", "two", "three")
        val newLine = "four"
        val expected = initialLines

        val mhm = MemoryHistoryManager.newInstance(
          maxLines = 0,
          initialLines = initialLines
        )
        mhm.writeLine(newLine)

        val actual = mhm.lines

        actual should be (expected)
      }

      it("should remove the oldest line if total lines exceeds maxLines") {
        val initialLines = Seq("one", "two", "three")
        val newLine = "four"
        val expected = initialLines.tail :+ newLine

        val mhm = MemoryHistoryManager.newInstance(
          maxLines = expected.length,
          initialLines = initialLines
        )
        mhm.writeLine(newLine)

        val actual = mhm.lines

        actual should be (expected)
      }
    }

    describe("#lines") {
      it("should return the lines of history held in memory") {
        val expected = Seq("one", "two", "three")

        val mhm = MemoryHistoryManager.newInstance()
        mhm.writeLines(expected: _*)

        val actual = mhm.lines

        actual should be (expected)
      }
    }

    describe("#destroy") {
      it("should clear the history held in memory") {
        val mhm = MemoryHistoryManager.newInstance()

        mhm.writeLines("one", "two", "three")

        mhm.destroy()

        mhm.lines should be (empty)
      }
    }
  }
}
