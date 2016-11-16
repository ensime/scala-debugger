package org.scaladebugger.api.profiles.scala210.info

import org.scaladebugger.api.profiles.pure.info.PureInfoProducerProfile

class Scala210InfoProducerProfileSpec extends test.ParallelMockFunSpec {
  private val scala210InfoProducerProfile = new Scala210InfoProducerProfile

  describe("Scala210InfoProducerProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        scala210InfoProducerProfile.toJavaInfo shouldBe
          a [PureInfoProducerProfile]
      }
    }

    describe("#isJavaInfo") {
      it("should return false") {
        val expected = false

        val actual = scala210InfoProducerProfile.isJavaInfo

        actual should be(expected)
      }
    }
  }
}
