package org.senkbeil.debugger.api.jdi.requests.filters

import com.sun.jdi.ObjectReference
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class InstanceFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockObjectReference = mock[ObjectReference]
  private val instanceFilter = InstanceFilter(
    objectReference = mockObjectReference
  )

  describe("InstanceFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the instance filter") {
        instanceFilter.toProcessor.argument should be (instanceFilter)
      }
    }
  }
}
