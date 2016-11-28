package org.scaladebugger.api.profiles.pure.info.events

import org.scaladebugger.api.profiles.traits.info.InfoProducerProfile

class PureEventInfoProducerProfileSpec extends test.ParallelMockFunSpec {
  private val mockInfoProducer = mock[InfoProducerProfile]
  private val pureEventInfoProducerProfile = new PureEventInfoProducerProfile(
    infoProducer = mockInfoProducer
  )

  describe("PureEventInfoProducerProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        // Also constructs its parent as its Java representation
        (mockInfoProducer.toJavaInfo _).expects()
          .returning(mockInfoProducer).once()

        pureEventInfoProducerProfile.toJavaInfo shouldBe a [PureEventInfoProducerProfile]
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureEventInfoProducerProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#infoProducer") {
      it("should be the parent passed into the event info producer") {
        pureEventInfoProducerProfile.infoProducer should
          be (mockInfoProducer)
      }
    }
  }
}
