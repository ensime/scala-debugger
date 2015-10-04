package org.senkbeil.debugger.jdi.requests.filters

import com.sun.jdi.ThreadReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class ThreadFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockThreadReference = mock[ThreadReference]
  private val threadFilter = ThreadFilter(threadReference = mockThreadReference)

  describe("ThreadFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the thread filter") {
        threadFilter.toProcessor.filter should be (threadFilter)
      }
    }
  }
}
