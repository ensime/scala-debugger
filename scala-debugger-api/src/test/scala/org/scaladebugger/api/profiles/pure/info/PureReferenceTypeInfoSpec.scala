package org.scaladebugger.api.profiles.pure.info

import com.sun.jdi._
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class PureReferenceTypeInfoSpec extends test.ParallelMockFunSpec
{
  private val mockNewFieldProfile = mockFunction[Field, Int, FieldVariableInfo]
  private val mockNewMethodProfile = mockFunction[Method, MethodInfo]
  private val mockNewLocationProfile = mockFunction[Location, LocationInfo]
  private val mockNewObjectProfile = mockFunction[ObjectReference, ObjectInfo]
  private val mockNewClassLoaderProfile = mockFunction[ClassLoaderReference, ClassLoaderInfo]
  private val mockNewClassObjectProfile = mockFunction[ClassObjectReference, ClassObjectInfo]
  private val mockNewReferenceTypeProfile = mockFunction[ReferenceType, ReferenceTypeInfo]
  private val mockScalaVirtualMachine = mock[ScalaVirtualMachine]
  private val mockInfoProducerProfile = mock[InfoProducer]
  private val mockReferenceType = mock[ReferenceType]
  private val pureReferenceTypeInfoProfile = new PureReferenceTypeInfo(
    scalaVirtualMachine = mockScalaVirtualMachine,
    infoProducer = mockInfoProducerProfile,
    _referenceType = mockReferenceType
  ) {
    override protected def newFieldProfile(
      field: Field, offsetIndex: Int
    ): FieldVariableInfo = mockNewFieldProfile(field, offsetIndex)

    override protected def newMethodProfile(
      method: Method
    ): MethodInfo = mockNewMethodProfile(method)

    override protected def newObjectProfile(
      objectReference: ObjectReference
    ): ObjectInfo = mockNewObjectProfile(objectReference)

    override protected def newLocationProfile(
      location: Location
    ): LocationInfo = mockNewLocationProfile(location)

    override protected def newClassObjectProfile(
      classObjectReference: ClassObjectReference
    ): ClassObjectInfo = mockNewClassObjectProfile(classObjectReference)

    override protected def newClassLoaderProfile(
      classLoaderReference: ClassLoaderReference
    ): ClassLoaderInfo = mockNewClassLoaderProfile(classLoaderReference)

    override protected def newReferenceTypeProfile(
      referenceType: ReferenceType
    ): ReferenceTypeInfo = mockNewReferenceTypeProfile(referenceType)
  }

  describe("PureReferenceTypeInfo") {
    describe("#toJavaInfo") {
      it("should return a new instance of the Java profile representation") {
        val expected = mock[ReferenceTypeInfo]

        // Get Java version of info producer
        (mockInfoProducerProfile.toJavaInfo _).expects()
          .returning(mockInfoProducerProfile).once()

        // Create new info profile using Java version of info producer
        (mockInfoProducerProfile.newReferenceTypeInfoProfile _)
          .expects(mockScalaVirtualMachine, mockReferenceType)
          .returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.toJavaInfo

        actual should be (expected)
      }
    }

    describe("#isJavaInfo") {
      it("should return true") {
        val expected = true

        val actual = pureReferenceTypeInfoProfile.isJavaInfo

        actual should be (expected)
      }
    }

    describe("#toJdiInstance") {
      it("should return the JDI instance this profile instance represents") {
        val expected = mockReferenceType

        val actual = pureReferenceTypeInfoProfile.toJdiInstance

        actual should be (expected)
      }
    }

    describe("#allFields") {
      it("should return a collection of profiles wrapping all fields in the underlying reference type") {
        val expected = Seq(mock[FieldVariableInfo])
        val fields = Seq(mock[Field])

        import scala.collection.JavaConverters._
        (mockReferenceType.allFields _).expects()
          .returning(fields.asJava).once()

        expected.zip(fields).foreach { case (e, f) =>
          mockNewFieldProfile.expects(f, -1).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.allFields

        actual should be (expected)
      }
    }

    describe("#visibleFields") {
      it("should return a collection of profiles wrapping visible fields in the underlying reference type") {
        val expected = Seq(mock[FieldVariableInfo])
        val fields = Seq(mock[Field])

        import scala.collection.JavaConverters._
        (mockReferenceType.visibleFields _).expects()
          .returning(fields.asJava).once()

        expected.zip(fields).foreach { case (e, f) =>
          mockNewFieldProfile.expects(f, -1).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.visibleFields

        actual should be (expected)
      }
    }

    describe("#fieldOption") {
      it("should return None if no field with matching name is found") {
        val expected = None

        val name = "someName"

        // Lookup the field and return null indicating no field found
        (mockReferenceType.fieldByName _).expects(name)
          .returning(null).once()

        val actual = pureReferenceTypeInfoProfile.fieldOption(name)

        actual should be (expected)
      }

      it("should return Some profile wrapping the associated field if found") {
        val expected = Some(mock[FieldVariableInfo])
        val name = "someName"

        // Lookup the field
        val mockField = mock[Field]
        (mockReferenceType.fieldByName _).expects(name)
          .returning(mockField).once()

        // Create the new profile
        mockNewFieldProfile.expects(mockField, -1).returning(expected.get).once()

        val actual = pureReferenceTypeInfoProfile.fieldOption(name)

        actual should be (expected)
      }
    }

    describe("#indexedVisibleFields") {
      it("should return a collection of profiles wrapping visible fields in the underlying reference type") {
        val expected = Seq(mock[FieldVariableInfo])
        val fields = Seq(mock[Field])

        import scala.collection.JavaConverters._
        (mockReferenceType.visibleFields _).expects()
          .returning(fields.asJava).once()

        expected.zip(fields).zipWithIndex.foreach { case ((e, f), i) =>
          mockNewFieldProfile.expects(f, i).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.indexedVisibleFields

        actual should be (expected)
      }
    }

    describe("#indexedFieldOption") {
      it("should return None if no field with matching name is found") {
        val expected = None
        val name = "someName"

        import scala.collection.JavaConverters._
        (mockReferenceType.visibleFields _).expects()
          .returning(Seq[Field]().asJava).once()

        val actual = pureReferenceTypeInfoProfile.indexedFieldOption(name)

        actual should be (expected)
      }

      it("should return Some profile wrapping the associated field if found") {
        val expected = Some(mock[FieldVariableInfo])
        val name = "someName"

        // Lookup the field
        val mockField = mock[Field]
        (expected.get.name _).expects().returning(name).once()

        import scala.collection.JavaConverters._
        (mockReferenceType.visibleFields _).expects()
          .returning(Seq(mockField).asJava).once()

        // Create the new profile
        mockNewFieldProfile.expects(mockField, 0).returning(expected.get).once()

        val actual = pureReferenceTypeInfoProfile.indexedFieldOption(name)

        actual should be (expected)
      }
    }

    describe("#allMethods") {
      it("should return a collection of profiles wrapping all methods in the underlying reference type") {
        val expected = Seq(mock[MethodInfo])
        val methods = Seq(mock[Method])

        import scala.collection.JavaConverters._
        (mockReferenceType.allMethods _).expects()
          .returning(methods.asJava).once()

        expected.zip(methods).foreach { case (e, m) =>
          mockNewMethodProfile.expects(m).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.allMethods

        actual should be (expected)
      }
    }

    describe("#visibleMethods") {
      it("should return a collection of profiles wrapping visible methods in the underlying reference type") {
        val expected = Seq(mock[MethodInfo])
        val methods = Seq(mock[Method])

        import scala.collection.JavaConverters._
        (mockReferenceType.visibleMethods _).expects()
          .returning(methods.asJava).once()

        expected.zip(methods).foreach { case (e, m) =>
          mockNewMethodProfile.expects(m).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.visibleMethods

        actual should be (expected)
      }
    }

    describe("#methods") {
      it("should return a collection of profiles wrapping methods with matching names in the underlying reference type") {
        val expected = Seq(mock[MethodInfo])
        val methods = Seq(mock[Method])
        val name = "someName"

        import scala.collection.JavaConverters._
        (mockReferenceType.methodsByName(_: String)).expects(name)
          .returning(methods.asJava).once()

        expected.zip(methods).foreach { case (e, m) =>
          mockNewMethodProfile.expects(m).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.methods(name)

        actual should be (expected)
      }
    }

    describe("#classLoaderOption") {
      it("should return Some profile wrapping the class loader of the reference type") {
        val expected = Some(mock[ClassLoaderInfo])
        val classLoader = mock[ClassLoaderReference]

        (mockReferenceType.classLoader _).expects()
          .returning(classLoader).once()

        mockNewClassLoaderProfile.expects(classLoader)
          .returning(expected.get).once()

        val actual = pureReferenceTypeInfoProfile.classLoaderOption

        actual should be (expected)
      }

      it("should return None if class loader is unavailable") {
        val expected = None

        (mockReferenceType.classLoader _).expects().returning(null).once()

        val actual = pureReferenceTypeInfoProfile.classLoaderOption

        actual should be (expected)
      }
    }

    describe("#classObject") {
      it("should return a profile wrapping the class object of the reference type") {
        val expected = mock[ClassObjectInfo]
        val classObject = mock[ClassObjectReference]

        (mockReferenceType.classObject _).expects()
          .returning(classObject).once()

        mockNewClassObjectProfile.expects(classObject)
          .returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.classObject

        actual should be (expected)
      }
    }

    describe("#genericSignature") {
      it("should return Some(signature) if it exists") {
        val expected = Some("signature")

        (mockReferenceType.genericSignature _).expects()
          .returning(expected.get).once()

        val actual = pureReferenceTypeInfoProfile.genericSignature

        actual should be (expected)
      }

      it("should return None if no signature exists") {
        val expected = None

        (mockReferenceType.genericSignature _).expects()
          .returning(null).once()

        val actual = pureReferenceTypeInfoProfile.genericSignature

        actual should be (expected)
      }
    }

    describe("#instances") {
      it("should throw an exception if a negative value is provided") {
        intercept[IllegalArgumentException] {
          pureReferenceTypeInfoProfile.instances(-1)
        }
      }

      it("should return a collection of profiles wrapping all instances returned from the underlying reference type") {
        val expected = Seq(mock[ObjectInfo])
        val objectReferences = Seq(mock[ObjectReference])

        import scala.collection.JavaConverters._
        (mockReferenceType.instances _).expects(0L)
          .returning(objectReferences.asJava).once()

        expected.zip(objectReferences).foreach { case (e, o) =>
          mockNewObjectProfile.expects(o).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.instances(0)

        actual should be (expected)
      }
    }

    describe("#isAbstract") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isAbstract _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isAbstract

        actual should be (expected)
      }
    }

    describe("#isFinal") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isFinal _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isFinal

        actual should be (expected)
      }
    }

    describe("#isInitialized") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isInitialized _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isInitialized

        actual should be (expected)
      }
    }

    describe("#isPrepared") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isPrepared _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isPrepared

        actual should be (expected)
      }
    }

    describe("#isVerified") {
      it("should return the state of the underlying reference type") {
        val expected = true

        (mockReferenceType.isVerified _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.isVerified

        actual should be (expected)
      }
    }

    describe("#allLineLocations") {
      it("should return a collection of profiles wrapping all line locations in the underlying reference type") {
        val expected = Seq(mock[LocationInfo])
        val locations = Seq(mock[Location])

        import scala.collection.JavaConverters._
        (mockReferenceType.allLineLocations: Function0[java.util.List[Location]])
          .expects()
          .returning(locations.asJava)
          .once()

        expected.zip(locations).foreach { case (e, l) =>
          mockNewLocationProfile.expects(l).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.allLineLocations

        actual should be (expected)
      }
    }

    describe("#locationsOfLine") {
      it("should return a collection of profiles wrapping all locations for the specified line in the underlying reference type") {
        val expected = Seq(mock[LocationInfo])
        val locations = Seq(mock[Location])
        val line = 999

        import scala.collection.JavaConverters._
        (mockReferenceType.locationsOfLine(_: Int)).expects(line)
          .returning(locations.asJava).once()

        expected.zip(locations).foreach { case (e, l) =>
          mockNewLocationProfile.expects(l).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.locationsOfLine(line)

        actual should be (expected)
      }
    }

    describe("#majorVersion") {
      it("should return the major version of the underlying reference type") {
        val expected = 999

        (mockReferenceType.majorVersion _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.majorVersion

        actual should be (expected)
      }
    }

    describe("#minorVersion") {
      it("should return the minor version of the underlying reference type") {
        val expected = 999

        (mockReferenceType.minorVersion _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.minorVersion

        actual should be (expected)
      }
    }

    describe("#name") {
      it("should return the name of the underlying reference type") {
        val expected = "some.class.name"

        (mockReferenceType.name _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.name

        actual should be (expected)
      }
    }

    describe("#signature") {
      it("should return the signature of the underlying reference type") {
        val expected = "signature"

        (mockReferenceType.signature _).expects().returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.signature

        actual should be (expected)
      }
    }

    describe("#nestedTypes") {
      it("should return a collection of profiles wrapping all nested types in the underlying reference type") {
        val expected = Seq(mock[ReferenceTypeInfo])
        val referenceTypes = Seq(mock[ReferenceType])

        import scala.collection.JavaConverters._
        (mockReferenceType.nestedTypes _).expects()
          .returning(referenceTypes.asJava).once()

        expected.zip(referenceTypes).foreach { case (e, r) =>
          mockNewReferenceTypeProfile.expects(r).returning(e).once()
        }

        val actual = pureReferenceTypeInfoProfile.nestedTypes

        actual should be (expected)
      }
    }

    describe("#sourceDebugExtension") {
      it("should return the source debug extension of the underlying reference type") {
        val expected = "debug"

        (mockReferenceType.sourceDebugExtension _).expects()
          .returning(expected).once()

        val actual = pureReferenceTypeInfoProfile.sourceDebugExtension

        actual should be (expected)
      }
    }

    describe("#sourceNames") {
      it("should return all source names for the underlying reference type") {
        val expected = Seq("file1.scala", "file2.scala")
        val stratumName = "some name"

        (mockReferenceType.defaultStratum _).expects()
          .returning(stratumName).once()

        import scala.collection.JavaConverters._
        (mockReferenceType.sourceNames _).expects(stratumName)
          .returning(expected.asJava).once()

        val actual = pureReferenceTypeInfoProfile.sourceNames

        actual should be (expected)
      }
    }

    describe("#sourcePaths") {
      it("should return all source paths for the underlying reference type") {
        val expected = Seq("path/to/file1.scala", "path/to/file2.scala")
        val stratumName = "some name"

        (mockReferenceType.defaultStratum _).expects()
          .returning(stratumName).once()

        import scala.collection.JavaConverters._
        (mockReferenceType.sourcePaths _).expects(stratumName)
          .returning(expected.asJava).once()

        val actual = pureReferenceTypeInfoProfile.sourcePaths

        actual should be (expected)
      }
    }
  }
}
