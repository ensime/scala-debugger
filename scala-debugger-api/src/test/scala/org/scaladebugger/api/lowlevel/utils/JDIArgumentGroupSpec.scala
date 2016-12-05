package org.scaladebugger.api.lowlevel.utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.lowlevel.JDIArgument
import org.scaladebugger.api.lowlevel.events.JDIEventArgument
import org.scaladebugger.api.lowlevel.requests.JDIRequestArgument

class JDIArgumentGroupSpec extends test.ParallelMockFunSpec
{
  describe("JDIArgumentGroup") {
    describe("#apply") {
      it("should place any request args in the request field of the group") {
        val expected = Seq(mock[JDIRequestArgument], mock[JDIRequestArgument])

        val JDIArgumentGroup(rArgs, _, _) = JDIArgumentGroup(expected: _*)

        val actual = rArgs

        actual should contain theSameElementsAs (expected)
      }

      it("should place any event args in the event field of the group") {
        val expected = Seq(mock[JDIEventArgument], mock[JDIEventArgument])

        val JDIArgumentGroup(_, eArgs, _) = JDIArgumentGroup(expected: _*)

        val actual = eArgs

        actual should contain theSameElementsAs (expected)
      }

      it("should place any other args in the other field of the group") {
        val expected = Seq(mock[JDIArgument], mock[JDIArgument])

        val JDIArgumentGroup(_, _, oArgs) = JDIArgumentGroup(expected: _*)

        val actual = oArgs

        actual should contain theSameElementsAs (expected)
      }

      it("should support grouping all types of arguments when mixed together") {
        val expectedRequestArgs = Seq(
          mock[JDIRequestArgument],
          mock[JDIRequestArgument]
        )
        val expectedEventArgs = Seq(
          mock[JDIEventArgument],
          mock[JDIEventArgument]
        )
        val expectedOtherArgs = Seq(
          mock[JDIArgument],
          mock[JDIArgument]
        )

        val JDIArgumentGroup(rArgs, eArgs, oArgs) = JDIArgumentGroup(
          expectedOtherArgs ++ expectedRequestArgs ++ expectedEventArgs: _*
        )

        rArgs should contain theSameElementsAs (expectedRequestArgs)
        eArgs should contain theSameElementsAs (expectedEventArgs)
        oArgs should contain theSameElementsAs (expectedOtherArgs)
      }
    }
  }
}
