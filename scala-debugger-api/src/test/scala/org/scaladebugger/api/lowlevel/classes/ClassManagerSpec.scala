package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.{Field, Method, ReferenceType, VirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers, ParallelTestExecution}
import test.JDIMockHelpers

import scala.collection.JavaConverters._
import scala.language.reflectiveCalls

class ClassManagerSpec extends FunSpec with Matchers with BeforeAndAfter
  with MockFactory with ParallelTestExecution with JDIMockHelpers
{
  private val stubVirtualMachine = stub[VirtualMachine]

  // TODO: Initialize with an abstracted class manager
  private val classManager = new StandardClassManager(
    stubVirtualMachine,
    loadClasses = false
  ) {
    @volatile var classMap: Map[String, Seq[ReferenceType]] = Map()
    override def classesWithName(className: String): Seq[ReferenceType] = {
      classMap.getOrElse(className, Nil)
    }
  }

  /** Sets classes for the specified classname. */
  private val setClassesWithName = (className: String, classes: Seq[ReferenceType]) => {
    classManager.classMap ++= Map(className -> classes)
  }

  /** Removes classes for the specified classname. */
  private val clearClassesWithName = (className: String) => {
    if (classManager.classMap.contains(className)) {
      classManager.classMap -= className
    }
  }

  /** Removes all classes from class manager classmap. */
  private val clearAllClasses = () => classManager.classMap = Map()

  describe("ClassManager") {
    describe("#hasClassWithName") {
      it("should return true if a class exists with the specified name") {
        val className = "some.class"
        val mockClasses = Seq(mock[ReferenceType])

        // Mark classes existing with our test name
        setClassesWithName(className, mockClasses)

        classManager.hasClassWithName(className) should be (true)
      }

      it("should return false if no class exists with the specified name") {
        val className = "some.class"

        // Mark classes not existing with our test name
        clearClassesWithName(className)

        classManager.hasClassWithName(className) should be (false)
      }
    }

    describe("#hasMethodWithName") {
      it("should return true if a method exists with the specified method name") {
        val className = "some.class"
        val methodName = "someMethod"
        val mockClass = mock[ReferenceType]
        val mockMethod = mock[Method]

        // Mark class existing with our test name
        setClassesWithName(className, Seq(mockClass))

        // Mark method to contain desired name
        (mockClass.allMethods _).expects().returning(Seq(mockMethod).asJava).once()
        (mockMethod.name _).expects().returning(methodName).once()

        classManager.hasMethodWithName(className, methodName) should be (true)
      }

      it("should return false if no method exists with the specified method name") {
        val className = "some.class"
        val methodName = "someMethod"
        val mockClass = mock[ReferenceType]
        val mockMethod = mock[Method]

        // Mark class existing with our test name
        setClassesWithName(className, Seq(mockClass))

        // Mark method to not contain desired name
        (mockClass.allMethods _).expects().returning(Seq(mockMethod).asJava).once()
        (mockMethod.name _).expects().returning(methodName + "wrong").once()

        classManager.hasMethodWithName(className, methodName) should be (false)
      }

      it("should return false if no class exists with the specified class name") {
        val className = "some.class"
        val methodName = "someMethod"

        // Mark classes not existing with our test name
        clearClassesWithName(className)

        classManager.hasMethodWithName(className, methodName) should be (false)
      }
    }

    describe("#methodsWithName") {
      it("should return methods matching the specified name for the class if it exists") {
        val className = "some.class"
        val methodName = "someMethod"
        val mockClass = mock[ReferenceType]
        val mockMethod = mock[Method]

        // Mark class existing with our test name
        setClassesWithName(className, Seq(mockClass))

        // Mark methods for class
        (mockClass.allMethods _).expects().returning(Seq(mockMethod).asJava).once()
        (mockMethod.name _).expects().returning(methodName).once()

        classManager.methodsWithName(className, methodName) should contain
          theSameElementsAs (Seq(mockMethod))
      }

      it("should return nothing if the class whose methods to check does not exist") {
        val className = "some.class"
        val methodName = "someMethod"

        // Mark classes not existing with our test name
        clearClassesWithName(className)

        classManager.methodsWithName(className, methodName) should be (empty)
      }
    }

    describe("#hasFieldWithName") {
      it("should return true if a field exists with the specified name") {
        val className = "some.class"
        val fieldName = "someField"
        val mockClass = mock[ReferenceType]
        val mockField = mock[Field]

        // Mark class existing with our test name
        setClassesWithName(className, Seq(mockClass))

        // Mark field to contain desired name
        (mockClass.allFields _).expects().returning(Seq(mockField).asJava).once()
        (mockField.name _).expects().returning(fieldName).once()

        classManager.hasFieldWithName(className, fieldName) should be (true)
      }

      it("should return false if no field exists with the specified name") {
        val className = "some.class"
        val fieldName = "someField"
        val mockClass = mock[ReferenceType]
        val mockField = mock[Field]

        // Mark class existing with our test name
        setClassesWithName(className, Seq(mockClass))

        // Mark field to not contain desired name
        (mockClass.allFields _).expects().returning(Seq(mockField).asJava).once()
        (mockField.name _).expects().returning(fieldName + "wrong").once()

        classManager.hasFieldWithName(className, fieldName) should be (false)
      }

      it("should return false if no class exists with the specified class name") {
        val className = "some.class"
        val fieldName = "someField"

        // Mark classes not existing with our test name
        clearClassesWithName(className)

        classManager.hasFieldWithName(className, fieldName) should be (false)
      }
    }

    describe("#fieldsWithName") {
      it("should return fields matching the specified name for the class if it exists") {
        val className = "some.class"
        val fieldName = "someField"
        val mockClass = mock[ReferenceType]
        val mockField = mock[Field]

        // Mark class existing with our test name
        setClassesWithName(className, Seq(mockClass))

        // Mark fields for class
        (mockClass.allFields _).expects().returning(Seq(mockField).asJava).once()
        (mockField.name _).expects().returning(fieldName).once()

        classManager.fieldsWithName(className, fieldName) should contain
          theSameElementsAs (Seq(mockField))
      }

      it("should return nothing if the class whose fields to check does not exist") {
        val className = "some.class"
        val fieldName = "someField"

        // Mark classes not existing with our test name
        clearClassesWithName(className)

        classManager.fieldsWithName(className, fieldName) should be (empty)
      }
    }

    describe("#allScalaFileNames") {
      it("should return all file names that have .scala as the extension") {
        val totalFileNamesPerExtension = 3
        val scalaFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".scala")
        val javaFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".java")
        val extFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".myExt")
        val otherFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".asdf")
        val fileNames =
          scalaFileNames ++ javaFileNames ++ extFileNames ++ otherFileNames

        val stubReferenceTypes = fileNames.map { fileName =>
          createReferenceTypeStub(
            name = "stub",
            sourcePaths = Seq(fileName),
            locations = Nil
          )
        }

        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify we have the right file names
        classManager.allScalaFileNames should
          contain theSameElementsAs scalaFileNames
      }
    }

    describe("#allJavaFileNames") {
      it("should return all file names that have .java as the extension") {
        val totalFileNamesPerExtension = 3
        val scalaFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".scala")
        val javaFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".java")
        val extFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".myExt")
        val otherFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".asdf")
        val fileNames =
          scalaFileNames ++ javaFileNames ++ extFileNames ++ otherFileNames

        val stubReferenceTypes = fileNames.map { fileName =>
          createReferenceTypeStub(
            name = "stub",
            sourcePaths = Seq(fileName),
            locations = Nil
          )
        }

        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify we have the right file names
        classManager.allJavaFileNames should
          contain theSameElementsAs javaFileNames
      }
    }

    describe("#allFileNamesWithExtension") {
      it("should return all file names that have the specified extension") {
        val totalFileNamesPerExtension = 3
        val scalaFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".scala")
        val javaFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".java")
        val extFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".myExt")
        val otherFileNames = (1 to totalFileNamesPerExtension)
          .map("test" + _ + ".asdf")
        val fileNames =
          scalaFileNames ++ javaFileNames ++ extFileNames ++ otherFileNames

        val stubReferenceTypes = fileNames.map { fileName =>
          createReferenceTypeStub(
            name = "stub",
            sourcePaths = Seq(fileName),
            locations = Nil
          )
        }

        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify we have the right file names
        classManager.allFileNamesWithExtension("myExt") should
          contain theSameElementsAs extFileNames
      }
    }
  }
}
