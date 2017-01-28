package org.scaladebugger.api.profiles.java.info

import org.scaladebugger.test.helpers.ParallelMockFunSpec

class JavaInfoProducerSpec extends ParallelMockFunSpec {
  private val javaInfoProducerProfile = new JavaInfoProducer

  describe("JavaInfoProducer") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        javaInfoProducerProfile.toJavaInfo shouldBe a [JavaInfoProducer]
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = javaInfoProducerProfile.isJavaInfo

        actual should be(expected)
      }
    }

    describe("#eventProducer") {
      it("should be a singleton that contains the info producer as a parent") {
        javaInfoProducerProfile.eventProducer.infoProducer should
          be (javaInfoProducerProfile)
      }
    }
  }
}
