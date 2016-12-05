package org.scaladebugger.api.utils

import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JavaProcessSpec extends FunSpec with Matchers with ParallelTestExecution {
  describe("JavaProcess") {
    describe("#fromJpsString") {
      it("should not allow null input") {
        intercept[IllegalArgumentException] {
          JavaProcess.fromJpsString(null)
        }
      }

      it("should return None when not enough arguments in string") {
        val expected = None

        val actual = JavaProcess.fromJpsString("1234")

        actual should be (expected)
      }

      it("should return None if first argument is not an integer") {
        val expected = None

        val actual = JavaProcess.fromJpsString("asdf asdf")

        actual should be (expected)
      }

      it("should use the first value as the pid of the process") {
        val expected = 1234

        val actual = JavaProcess.fromJpsString(s"$expected some.class.name")

        actual.get.pid should be (expected)
      }

      it("should use the second value as the fully-qualified class name") {
        val expected = "some.class.name"

        val actual = JavaProcess.fromJpsString(s"1234 $expected")

        actual.get.className should be (expected)
      }

      it("should parse any additional arguments as JVM options") {
        val expected = JVMOptions.Blank
        val extraArguments = "-Dsome.key=value -asdf"

        val actual = JavaProcess.fromJpsString(
          s"1234 some.class.name $extraArguments",
          s => {
            s should be (extraArguments)
            expected
          }
        )

        actual.get.jvmOptions should be (expected)
      }
    }
  }
}
