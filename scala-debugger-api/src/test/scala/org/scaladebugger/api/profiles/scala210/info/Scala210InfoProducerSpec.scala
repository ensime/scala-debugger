package org.scaladebugger.api.profiles.scala210.info

import org.scaladebugger.api.profiles.pure.info.PureInfoProducer

class Scala210InfoProducerSpec extends test.ParallelMockFunSpec {
  private val scala210InfoProducerProfile = new Scala210InfoProducer

  describe("Scala210InfoProducer") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        scala210InfoProducerProfile.toJavaInfo shouldBe
          a [PureInfoProducer]
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
