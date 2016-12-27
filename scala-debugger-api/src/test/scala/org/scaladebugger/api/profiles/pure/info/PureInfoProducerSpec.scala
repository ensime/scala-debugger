package org.scaladebugger.api.profiles.pure.info

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class PureInfoProducerSpec extends ParallelMockFunSpec {
  private val pureInfoProducerProfile = new PureInfoProducer

  describe("PureInfoProducer") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        pureInfoProducerProfile.toJavaInfo shouldBe a [PureInfoProducer]
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureInfoProducerProfile.isJavaInfo

        actual should be(expected)
      }
    }

    describe("#eventProducer") {
      it("should be a singleton that contains the info producer as a parent") {
        pureInfoProducerProfile.eventProducer.infoProducer should
          be (pureInfoProducerProfile)
      }
    }
  }
}
