package org.scaladebugger.api.profiles.pure.info

class PureInfoProducerProfileSpec extends test.ParallelMockFunSpec {
  private val pureInfoProducerProfile = new PureInfoProducerProfile

  describe("PureInfoProducerProfile") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        pureInfoProducerProfile.toJavaInfo shouldBe a [PureInfoProducerProfile]
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureInfoProducerProfile.isJavaInfo

        actual should be(expected)
      }
    }
  }
}
