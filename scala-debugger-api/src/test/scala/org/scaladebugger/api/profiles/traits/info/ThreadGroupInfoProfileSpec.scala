package org.scaladebugger.api.profiles.traits.info

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import test.InfoTestClasses.TestThreadGroupInfoProfile

import scala.util.{Success, Try}

class ThreadGroupInfoProfileSpec extends test.ParallelMockFunSpec
{
  describe("ThreadGroupInfoProfile") {
    describe("#toPrettyString") {
      it("should display the thread name and unique id as a hex code") {
        val expected = "Thread Group threadGroupName (0xABCDE)"

        val threadInfoProfile = new TestThreadGroupInfoProfile {
          override def uniqueId: Long = Integer.parseInt("ABCDE", 16)
          override def name: String = "threadGroupName"
        }

        val actual = threadInfoProfile.toPrettyString

        actual should be(expected)
      }
    }
  }
}
