package org.scaladebugger.api.utils

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class JVMOptionsSpec extends ParallelMockFunSpec {
  describe("JVMOptions") {
    describe("#fromOptionString") {
      it("should not allow null input") {
        intercept[IllegalArgumentException] {
          JVMOptions.fromOptionString(null)
        }
      }

      it("should return blank options if the input is empty") {
        val expected = JVMOptions.Blank

        val actual = JVMOptions.fromOptionString("")

        actual should be (expected)
      }

      it("should parse -Dkey=value as a property") {
        val key = "some.key"
        val value = "some.value"
        val expected = JVMOptions(
          properties = Map(key -> value),
          options = Map()
        )

        val actual = JVMOptions.fromOptionString(s"-D$key=$value")

        actual should be (expected)
      }

      it("should parse normal arguments as options") {
        val key = "some.key"
        val value = "some.value"
        val expected = JVMOptions(
          properties = Map(),
          options = Map(key -> value)
        )

        val actual = JVMOptions.fromOptionString(s"--$key=$value")

        actual should be (expected)
      }

      it("should remove - from -key") {
        val key = "key"
        val expected = JVMOptions(
          properties = Map(),
          options = Map(key -> "")
        )

        val actual = JVMOptions.fromOptionString(s"-$key")

        actual should be (expected)
      }

      it("should remove -- from --key") {
        val key = "some-key"
        val expected = JVMOptions(
          properties = Map(),
          options = Map(key -> "")
        )

        val actual = JVMOptions.fromOptionString(s"--$key")

        actual should be (expected)
      }

      it("should fill in any option without a value with an empty string") {
        val key = "key"
        val expected = JVMOptions(
          properties = Map(),
          options = Map(key -> "")
        )

        val actual = JVMOptions.fromOptionString(s"$key")

        actual should be (expected)
      }

      it("should parse quotes to set the value to content inside quotes") {
        val key = "some-key"
        val value = "some-value"
        val expected = JVMOptions(
          properties = Map(),
          options = Map(key -> value)
        )

        val q = "\""
        val actual = JVMOptions.fromOptionString(s"--$key=$q$value$q")

        actual should be (expected)
      }

      it("should support quotes containing spaces") {
        val key = "some-key"
        val value = "some spaced value"
        val expected = JVMOptions(
          properties = Map(),
          options = Map(key -> value)
        )

        val q = "\""
        val actual = JVMOptions.fromOptionString(s"--$key=$q$value$q")

        actual should be (expected)
      }
    }
  }
}
