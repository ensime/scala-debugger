package org.senkbeil.debugger.api.jdi.requests.filters

import com.sun.jdi.ReferenceType
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}

class ClassReferenceFilterSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
{
  private val mockReferenceType = mock[ReferenceType]
  private val classReferenceFilter = ClassReferenceFilter(
    referenceType = mockReferenceType
  )

  describe("ClassReferenceFilter") {
    describe("#toProcessor") {
      it("should return a processor containing the class reference filter") {
        classReferenceFilter.toProcessor.argument should be (classReferenceFilter)
      }
    }
  }
}
