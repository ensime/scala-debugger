package org.senkbeil.debugger.api.wrappers

import com.sun.jdi.{Field, ClassNotPreparedException, ReferenceType}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}
import test.JDIMockHelpers

import scala.collection.JavaConverters._

class ReferenceTypeWrapperSpec extends FunSpec with Matchers with MockFactory
  with JDIMockHelpers with OneInstancePerTest
{
  private val mockReferenceType = mock[ReferenceType]
  private val referenceTypeWrapper =
    new ReferenceTypeWrapper(mockReferenceType)

  describe("ReferenceTypeWrapper") {
    describe("constructor") {
      it("should throw an exception if wrapping a null pointer") {
        intercept[IllegalArgumentException] {
          new ReferenceTypeWrapper(null)
        }
      }
    }

    describe("#staticFieldsAndValues") {
      it("should return a collection of fields whose values can be retrieved") {
        val fieldsPerType = 3

        // Create static fields
        val staticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"static$i"))
        staticFields.foreach(field => (field.isStatic _).when().returns(true))
        val staticFieldsAndValues = staticFields
          .map((_, createPrimitiveValueStub(0)))
          .toMap

        // Create static fields that will fail to be retrieved
        val badStaticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"bad-static$i"))
        badStaticFields.foreach(field =>
          (field.isStatic _).when().returns(true))

        // Create normal fields
        val nonStaticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"normal$i"))
        nonStaticFields.foreach(field =>
          (field.isStatic _).when().returns(false))

        // Return static and non-static fields
        (mockReferenceType.allFields _).expects().returning(
          (staticFields ++ badStaticFields ++ nonStaticFields).asJava)

        // Only allow static fields through
        (mockReferenceType.getValue _).expects(*).onCall { (field: Field) =>
          // If bad field, throw exception
          if (badStaticFields.contains(field)) {
            throw new IllegalArgumentException

            // If good field, return value
          } else {
            staticFieldsAndValues(field)
          }
        } repeated (staticFields ++ badStaticFields).length times()

        val expected = staticFieldsAndValues.toSeq
        val actual = referenceTypeWrapper.staticFieldsAndValues

        // Should contain ONLY the static fields
        actual should contain theSameElementsAs expected
      }

      it("should return an empty collection if unable to retrieve fields") {
        (mockReferenceType.allFields _).expects()
          .throwing(new ClassNotPreparedException())

        referenceTypeWrapper.staticFieldsAndValues should be (empty)
      }

      it("should skip fields whose values cannot be retrieved") {
        val fieldsPerType = 3

        // Create static fields
        val staticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"static$i"))
        staticFields.foreach(field => (field.isStatic _).when().returns(true))
        val staticFieldsAndValues = staticFields
          .map((_, createPrimitiveValueStub(0)))
          .toMap

        // Create static fields that will fail to be retrieved
        val badStaticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"bad-static$i"))
        badStaticFields.foreach(field =>
          (field.isStatic _).when().returns(true))

        // Create normal fields
        val nonStaticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"normal$i"))
        nonStaticFields.foreach(field =>
          (field.isStatic _).when().returns(false))

        // Return static and non-static fields
        (mockReferenceType.allFields _).expects().returning(
          (staticFields ++ badStaticFields ++ nonStaticFields).asJava)

        // Only allow static fields through
        (mockReferenceType.getValue _).expects(*).onCall { (field: Field) =>
          // If bad field, throw exception
          if (badStaticFields.contains(field)) {
            throw new IllegalArgumentException

          // If good field, return value
          } else {
            staticFieldsAndValues(field)
          }
        } repeated (staticFields ++ badStaticFields).length times()

        // Should not contain any bad static fields
        val actualFields = referenceTypeWrapper.staticFieldsAndValues.map(_._1)

        // TODO: Figure out why cannot use "should contain noneOf ..."
        actualFields.foreach { field =>
          badStaticFields should not contain (field)
        }
      }
    }

    describe("#staticFields") {
      it("should return a collection of static fields") {
        val fieldsPerType = 3

        // Create static fields
        val staticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"static$i"))
        staticFields.foreach(field => (field.isStatic _).when().returns(true))

        // Create normal fields
        val nonStaticFields = (1 to fieldsPerType)
          .map(i => createFieldStub(s"normal$i"))
        nonStaticFields.foreach(field =>
          (field.isStatic _).when().returns(false))

        // Return static and non-static fields
        (mockReferenceType.allFields _).expects()
          .returning((staticFields ++ nonStaticFields).asJava)

        // Should contain ONLY the static fields
        referenceTypeWrapper.staticFields should
          contain theSameElementsAs staticFields
      }

      it("should return an empty collection if unable to retrieve fields") {
        (mockReferenceType.allFields _).expects()
          .throwing(new ClassNotPreparedException())

        referenceTypeWrapper.staticFields should be (empty)
      }
    }
  }
}
