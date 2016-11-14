package org.scaladebugger.api.dsl.info

import com.sun.jdi.VirtualMachine
import org.scaladebugger.api.lowlevel.ManagerContainer
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.profiles.traits.DebugProfile
import org.scaladebugger.api.profiles.traits.info.{ObjectInfoProfile, ValueInfoProfile}
import org.scaladebugger.api.virtualmachines.{ObjectCache, ScalaVirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class ValueInfoDSLWrapperSpec extends test.ParallelMockFunSpec
{
  private val TestUniqueId = 1234L
  private val mockObjectInfoProfile = mock[ObjectInfoProfile]
  private val mockValueInfoProfile = mock[ValueInfoProfile]

  private val testObjectCache = new ObjectCache()
  private val testScalaVirtualMachine = new Object with ScalaVirtualMachine {
    override val cache: ObjectCache = testObjectCache
    override val lowlevel: ManagerContainer = null
    override def startProcessingEvents(): Unit = {}
    override def isInitialized: Boolean = false
    override def isProcessingEvents: Boolean = false
    override def suspend(): Unit = {}
    override def stopProcessingEvents(): Unit = {}
    override def resume(): Unit = {}
    override def initialize(
      defaultProfile: String,
      startProcessingEvents: Boolean
    ): Unit = {}
    override val underlyingVirtualMachine: VirtualMachine = null
    override def isStarted: Boolean = false
    override val uniqueId: String = ""
    override protected val profileManager: ProfileManager = null
    override def register(
      name: String,
      profile: DebugProfile
    ): Option[DebugProfile] = None
    override def retrieve(name: String): Option[DebugProfile] = None
    override def unregister(name: String): Option[DebugProfile] = None
  }

  describe("ValueInfoDSLWrapper") {
    describe("#cache") {
      it("should add the value, if an object, to the implicit cache if available") {
        import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).once()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        implicit val objectCache: ObjectCache = testObjectCache
        mockObjectInfoProfile.cache()

        objectCache.has(TestUniqueId) should be (true)
      }

      it("should add the value, if an object, to underlying JVM's cache if no implicit available") {
        import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL

        (mockObjectInfoProfile.scalaVirtualMachine _).expects()
          .returning(testScalaVirtualMachine).once()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).once()

        mockObjectInfoProfile.cache()

        testObjectCache.has(TestUniqueId) should be (true)
      }

      it("should do nothing if the value is not an object") {
        import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL

        (mockValueInfoProfile.scalaVirtualMachine _).expects()
          .returning(testScalaVirtualMachine).once()

        (mockValueInfoProfile.isObject _).expects().returning(false).once()

        mockValueInfoProfile.cache()

        testObjectCache.has(TestUniqueId) should be (false)
      }
    }

    describe("#uncache") {
      it("should remove the value, if an object, from the implicit cache if available") {
        import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).twice()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        implicit val objectCache: ObjectCache = testObjectCache
        objectCache.save(mockObjectInfoProfile)

        mockObjectInfoProfile.uncache()

        objectCache.has(TestUniqueId) should be (false)
      }

      it("should remove the value, if an object, from underlying JVM's cache if no implicit available") {
        import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL

        (mockObjectInfoProfile.scalaVirtualMachine _).expects()
          .returning(testScalaVirtualMachine).once()

        (mockObjectInfoProfile.isObject _).expects().returning(true).once()
        (mockObjectInfoProfile.toObjectInfo _).expects()
          .returning(mockObjectInfoProfile).once()

        (mockObjectInfoProfile.uniqueId _).expects()
          .returning(TestUniqueId).twice()

        testObjectCache.save(mockObjectInfoProfile)
        mockObjectInfoProfile.uncache()

        testObjectCache.has(TestUniqueId) should be (false)
      }

      it("should do nothing if the value is not an object") {
        import org.scaladebugger.api.dsl.Implicits.ValueInfoDSL

        (mockValueInfoProfile.scalaVirtualMachine _).expects()
          .returning(testScalaVirtualMachine).once()

        (mockValueInfoProfile.isObject _).expects().returning(false).once()

        mockValueInfoProfile.uncache()

        testObjectCache.has(TestUniqueId) should be (false)
      }
    }
  }
}
