package org.scaladebugger.api.utils

import java.nio.file.{Path, Paths}

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class FileSearcherIntegrationSpec extends FunSpec with Matchers
  with BeforeAndAfter with VirtualMachineFixtures
  with TestUtilities with Eventually
  with ParallelTestExecution
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  private val fileSearcher = new FileSearcher
  private lazy val searchPath = Paths.get(
    ClassLoader.getSystemResource("file_searcher").toURI
  )

  describe("FileSearcher") {
    describe("#newGlobMatcher") {
      it("should return a matcher using the glob syntax") {
        // Match recursively for java files
        val globMatcher = fileSearcher.newGlobMatcher("**.java")

        val validPath = Paths.get("some/path/with/myfile.java")
        globMatcher.matches(validPath) should be (true)

        val invalidPath = Paths.get("some/other/path/with/myfile.other")
        globMatcher.matches(invalidPath) should be (false)
      }
    }

    describe("#loadFilePaths") {
      it("should be able to find all files in a directory and subdirectories") {
        val paths = fileSearcher.loadFilePaths(searchPath)
        val pathStrings = paths.map(_.toString)

        pathStrings should contain theSameElementsAs Seq(
          searchPath.resolve("file1.txt"),
          searchPath.resolve("file2.other"),
          searchPath.resolve("subdir/file1.txt"),
          searchPath.resolve("subdir/file2.other"),
          searchPath.resolve("subdir/file3.txt"),
          searchPath.resolve("subdir/file4.other")
        ).map(_.toString)
      }

      it("should be able to limit returned files using a path matcher") {
        val globMatcher = fileSearcher.newGlobMatcher("**.txt")
        val paths = fileSearcher.loadFilePaths(
          searchPath,
          pathMatcher = Some(globMatcher)
        )
        val pathStrings = paths.map(_.toString)

        pathStrings should contain theSameElementsAs Seq(
          searchPath.resolve("file1.txt"),
          searchPath.resolve("subdir/file1.txt"),
          searchPath.resolve("subdir/file3.txt")
        ).map(_.toString)
      }

      it("should use a glob path matcher if provided a glob string") {
        val paths = fileSearcher.loadFilePaths(
          searchPath,
          globString = "**.txt"
        )
        val pathStrings = paths.map(_.toString)

        pathStrings should contain theSameElementsAs Seq(
          searchPath.resolve("file1.txt"),
          searchPath.resolve("subdir/file1.txt"),
          searchPath.resolve("subdir/file3.txt")
        ).map(_.toString)
      }

      it("should be able to limit searching by the provided search depth") {
        // Limit to just contents of specified directory
        val paths = fileSearcher.loadFilePaths(searchPath, maxDepth = 1)
        val pathStrings = paths.map(_.toString)

        pathStrings should contain theSameElementsAs Seq(
          searchPath.resolve("file1.txt"),
          searchPath.resolve("file2.other")
        ).map(_.toString)
      }
    }
  }
}
