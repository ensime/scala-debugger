package org.scaladebugger.api.virtualmachines

import com.sun.jdi.{ObjectReference, VMCannotBeModifiedException}
import org.scaladebugger.api.profiles.traits.info.ObjectInfo
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class ObjectCacheSpec extends test.ParallelMockFunSpec
{
  private val TestUniqueId = 1234L
  private val internalCache =
    new collection.mutable.HashMap[Long, ObjectInfo]()
  private val objectCache = new ObjectCache(internalCache)

  describe("ObjectCache") {
    describe("#save") {
      it("should cache the object using its unique id") {
        val obj = mock[ObjectInfo]
        (obj.uniqueId _).expects().returning(TestUniqueId).once()

        objectCache.save(obj)

        internalCache.keySet should contain (TestUniqueId)
      }

      it("should return Some(id) if successfully cached") {
        val expected = Some(TestUniqueId)

        val obj = mock[ObjectInfo]
        (obj.uniqueId _).expects().returning(TestUniqueId).once()

        val actual = objectCache.save(obj)

        actual should be (expected)
      }

      it("should return None if unable to retrieve the id to cache the object") {
        val expected = None

        val obj = mock[ObjectInfo]
        (obj.uniqueId _).expects().throwing(new Throwable).once()

        val actual = objectCache.save(obj)

        actual should be (expected)
      }
    }

    describe("#load") {
      it("should return None if no object with matching id is cached") {
        val expected = None

        val actual = objectCache.load(TestUniqueId)

        actual should be (expected)
      }

      it("should return None and remove the cached object if it was collected") {
        val expected = None

        val obj = mock[ObjectInfo]
        internalCache.put(TestUniqueId, obj)

        val mockObjectReference = mock[ObjectReference]
        (obj.toJdiInstance _).expects().returning(mockObjectReference).once()

        (mockObjectReference.isCollected _).expects().returning(true).once()

        val actual = objectCache.load(TestUniqueId)

        actual should be (expected)
        internalCache.get(TestUniqueId) should be (None)
      }

      it("should return None and remove the cached object if failed to determine if collected") {
        val expected = None

        val obj = mock[ObjectInfo]
        internalCache.put(TestUniqueId, obj)

        val mockObjectReference = mock[ObjectReference]
        (obj.toJdiInstance _).expects().returning(mockObjectReference).once()

        (mockObjectReference.isCollected _).expects()
          .throwing(new VMCannotBeModifiedException()).once()

        val actual = objectCache.load(TestUniqueId)

        actual should be (expected)
        internalCache.get(TestUniqueId) should be (None)
      }

      it("should return Some(object) if the cached object was not collected") {
        val expected = Some(mock[ObjectInfo])

        val obj = expected.get
        internalCache.put(TestUniqueId, obj)

        val mockObjectReference = mock[ObjectReference]
        (obj.toJdiInstance _).expects().returning(mockObjectReference).once()

        (mockObjectReference.isCollected _).expects().returning(false).once()

        val actual = objectCache.load(TestUniqueId)

        actual should be (expected)
        internalCache.get(TestUniqueId) should be (expected)
      }
    }

    describe("#has(id)") {
      it("should return true if an object with the provided id is in the cache") {
        val expected = true

        internalCache.put(TestUniqueId, mock[ObjectInfo])

        val actual = objectCache.has(TestUniqueId)

        actual should be (expected)
      }

      it("should return false if no object with the provided id is in the cache") {
        val expected = false

        val actual = objectCache.has(TestUniqueId)

        actual should be (expected)
      }
    }

    describe("#has(object)") {
      it("should return true if the object with the provided id is in the cache") {
        val expected = true

        val mockObject = mock[ObjectInfo]
        internalCache.put(TestUniqueId, mockObject)

        (mockObject.uniqueId _).expects().returning(TestUniqueId).once()

        val actual = objectCache.has(mockObject)

        actual should be (expected)
      }

      it("should return false if unable to get the unique id from the object") {
        val expected = false

        val mockObject = mock[ObjectInfo]
        internalCache.put(TestUniqueId, mockObject)

        (mockObject.uniqueId _).expects()
          .throwing(new VMCannotBeModifiedException()).once()

        val actual = objectCache.has(mockObject)

        actual should be (expected)
      }

      it("should return false if no object with the provided id is in the cache") {
        val expected = false

        val mockObject = mock[ObjectInfo]
        (mockObject.uniqueId _).expects().returning(TestUniqueId).once()

        val actual = objectCache.has(mockObject)

        actual should be (expected)
      }
    }

    describe("#remove(id)") {
      it("should remove the cached object and return Some(object)") {
        val expected = Some(mock[ObjectInfo])

        internalCache.put(TestUniqueId, expected.get)

        val actual = objectCache.remove(TestUniqueId)

        actual should be (expected)
      }

      it("should return None if no object with the id is cached") {
        val expected = None

        val actual = objectCache.remove(TestUniqueId)

        actual should be (expected)
      }
    }

    describe("#remove(object)") {
      it("should remove the cached object and return Some(object)") {
        val expected = Some(mock[ObjectInfo])

        val mockObject = expected.get
        internalCache.put(TestUniqueId, mockObject)

        (mockObject.uniqueId _).expects().returning(TestUniqueId).once()

        val actual = objectCache.remove(mockObject)

        actual should be (expected)
        internalCache.values should not contain (mockObject)
      }

      it("should return None if unable to get the unique id of the object") {
        val expected = None

        val mockObject = mock[ObjectInfo]
        internalCache.put(TestUniqueId, mockObject)

        (mockObject.uniqueId _).expects()
          .throwing(new VMCannotBeModifiedException()).once()

        val actual = objectCache.remove(mockObject)

        actual should be (expected)
        internalCache.values should contain (mockObject)
      }

      it("should return None if no object with the id is cached") {
        val expected = None

        val mockObject = mock[ObjectInfo]

        (mockObject.uniqueId _).expects().returning(TestUniqueId).once()

        val actual = objectCache.remove(mockObject)

        actual should be (expected)
      }
    }

    describe("#clear") {
      it("should remove all cached objects") {
        internalCache.put(TestUniqueId, mock[ObjectInfo])
        internalCache.put(TestUniqueId + 1, mock[ObjectInfo])
        internalCache.put(TestUniqueId + 2, mock[ObjectInfo])

        objectCache.clear()

        internalCache should be (empty)
      }
    }
  }
}
