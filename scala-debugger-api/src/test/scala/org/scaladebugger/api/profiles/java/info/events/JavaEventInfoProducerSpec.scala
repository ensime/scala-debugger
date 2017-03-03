package org.scaladebugger.api.profiles.java.info.events

import org.scaladebugger.api.profiles.traits.info.InfoProducer
import org.scaladebugger.test.helpers.ParallelMockFunSpec

class JavaEventInfoProducerSpec extends ParallelMockFunSpec {
  private val mockInfoProducer = mock[InfoProducer]
  private val javaEventInfoProducerProfile = new JavaEventInfoProducer(
    infoProducer = mockInfoProducer
  )

  describe("JavaEventInfoProducer") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        // Also constructs its parent as its Java representation
        (mockInfoProducer.toJavaInfo _).expects()
          .returning(mockInfoProducer).once()

        javaEventInfoProducerProfile.toJavaInfo shouldBe a [JavaEventInfoProducer]
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaEventInfoProducerProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#infoProducer") {
      it("should be the parent passed into the event info producer") {
        javaEventInfoProducerProfile.infoProducer should
          be (mockInfoProducer)
      }
    }
  }
}
