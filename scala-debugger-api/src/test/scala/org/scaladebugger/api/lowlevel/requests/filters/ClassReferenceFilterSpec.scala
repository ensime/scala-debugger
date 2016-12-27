package org.scaladebugger.api.lowlevel.requests.filters

import com.sun.jdi.ReferenceType
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class ClassReferenceFilterSpec extends ParallelMockFunSpec
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
