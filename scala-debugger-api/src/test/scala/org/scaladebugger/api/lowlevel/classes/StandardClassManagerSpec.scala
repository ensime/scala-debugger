package org.scaladebugger.api.lowlevel.classes

import com.sun.jdi.{ReferenceType, VirtualMachine}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.JDIMockHelpers

import scala.collection.JavaConverters._

class StandardClassManagerSpec extends ParallelMockFunSpec with JDIMockHelpers {
  private val TotalMockReferenceTypes = 5

  private def indexToExtension(index: Int) =
    if (index % 3 == 0)       "scala"
    else if (index % 3 == 1)  "java"
    else                      "unknown"

  private val stubReferenceTypes = (1 to TotalMockReferenceTypes).map { i =>
    createRandomReferenceTypeStub(indexToExtension(i))
  }

  private val stubVirtualMachine = stub[VirtualMachine]

  private val classManager =
    new StandardClassManager(stubVirtualMachine, loadClasses = false)

  describe("StandardClassManager") {
    describe("constructor") {
      it("should refresh the class listing if told to load classes") {
        // Set up the virtual machine to return the generated classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Create a class manager that automatically loads classes
        val classManager =
          new StandardClassManager(stubVirtualMachine, loadClasses = true)

        classManager.allFileNames should contain theSameElementsAs
          stubReferenceTypes.map(_.sourcePaths("").asScala.head)
      }

      it("should not refresh the class listing if flag is set to false") {
        val classManager =
          new StandardClassManager(stubVirtualMachine, loadClasses = false)

        classManager.allFileNames shouldBe empty
      }
    }

    describe("#linesAndLocationsForFile") {
      it("should return Some(map containing lines and associated locations for the file)") {
        val fileName = "test.scala"
        val locations = Seq(
          createRandomLocationStub(),
          createRandomLocationStub(),
          createRandomLocationStub()
        )

        // Create our class with two locations
        val stubReferenceType = createReferenceTypeStub(
          name = "some reference type",
          sourcePaths = Seq(fileName),
          locations = locations
        )

        // Set up the virtual machine to return that class
        (stubVirtualMachine.allClasses _).when()
          .returns(Seq(stubReferenceType).asJava)

        // Load our classes
        classManager.refreshAllClasses()

        val expected = locations.groupBy(_.lineNumber())
        val actual = classManager.linesAndLocationsForFile(fileName).get

        actual should contain theSameElementsAs expected
      }

      it("should not include locations whose line numbers cannot be retrieved") {
        val fileName = "test.scala"
        val goodLocation = createLocationStub(3, throwException = false)
        val badLocation = createLocationStub(4, throwException = true)

        // Create our class with two locations
        val stubReferenceType = createReferenceTypeStub(
          name = "some reference type",
          sourcePaths = Seq(fileName),
          locations = Seq(goodLocation, badLocation)
        )

        // Set up the virtual machine to return that class
        (stubVirtualMachine.allClasses _).when()
          .returns(Seq(stubReferenceType).asJava)

        // Load our classes
        classManager.refreshAllClasses()

        val expected = Seq(goodLocation).groupBy(_.lineNumber())
        val actual = classManager.linesAndLocationsForFile(fileName).get

        actual should contain theSameElementsAs expected
      }

      it("should return None if the file is not found in the cache") {
        classManager.linesAndLocationsForFile("asdf") should be (None)
      }
    }

    describe("#classesWithName") {
      it("should return all classes whose name matches the specified name") {
        val expected = Seq(mock[ReferenceType], mock[ReferenceType])

        val className = "some.class.name"

        // Delegates to underlying virtual machine method to check classes
        (stubVirtualMachine.classesByName _).when(className)
          .returns(expected.asJava).once()

        val actual = classManager.classesWithName(className)

        actual should be (expected)
      }
    }

    describe("#underlyingReferencesFor") {
      it("should return Some(collection of reference types matching the filename found in the cache)") {
        val fileName = "test.scala"

        // Create our classes with file name
        val stubReferenceTypes = Seq(
          createReferenceTypeStub(
            name = "some reference type",
            sourcePaths = Seq(fileName),
            locations = Nil
          ),
          createReferenceTypeStub(
            name = "some other reference type",
            sourcePaths = Seq(fileName),
            locations = Nil
          )
        )

        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify that we have the classes available
        classManager.underlyingReferencesForFile(fileName).get should
          contain theSameElementsAs stubReferenceTypes
      }

      it("should return None if the filename is not found in the cache") {
        classManager.underlyingReferencesForFile("does not exist") should
          be (None)
      }
    }

    describe("#refreshClass") {
      it("should make the class the first for the file if none exist") {
        val fileName = "somefile.scala"
        val stubReferenceType = createReferenceTypeStub(
          name = "stub",
          sourcePaths = Seq(fileName),
          locations = Nil
        )

        classManager.refreshClass(stubReferenceType)

        val expected = Seq(stubReferenceType)
        val actual = classManager.underlyingReferencesForFile(fileName).get

        actual should contain theSameElementsAs expected
      }

      it("should add the class to the existing collection of classes") {
        val referencesPerFile = 3
        val fileName = "somefile.scala"
        val stubReferenceTypes = (1 to referencesPerFile).map(i =>
          createReferenceTypeStub(
            name = "stub" + i,
            sourcePaths = Seq(fileName),
            locations = Nil
          )
        )

        stubReferenceTypes.foreach(classManager.refreshClass)

        val expected = stubReferenceTypes
        val actual = classManager.underlyingReferencesForFile(fileName).get

        actual should contain theSameElementsAs expected
      }

      it("should add the class to ARRAY if is an array") {
        val stubReferenceType = createReferenceTypeStub(
          name = "somearray[]",
          sourcePaths = Seq("a", "b"),
          locations = Nil
        )

        classManager.refreshClass(stubReferenceType)

        val expected = Seq(stubReferenceType)
        val actual = classManager.underlyingReferencesForFile("ARRAY").get

        actual should contain theSameElementsAs expected
      }

      it("should add the class to UNKNOWN if it has no file name and not an array") {
        val stubReferenceType = createReferenceTypeStub(
          name = "someunknown",
          sourcePaths = Seq("a", "b"),
          locations = Nil
        )

        classManager.refreshClass(stubReferenceType)

        val expected = Seq(stubReferenceType)
        val actual = classManager.underlyingReferencesForFile("UNKNOWN").get

        actual should contain theSameElementsAs expected
      }
    }

    describe("#refreshAllClasses") {
      it("should group classes by file name") {
        val referencesPerFile = 3
        val fileNames = Seq(
          "test1.scala",
          "test2.scala",
          "test3.scala"
        )

        // Create our classes with file names
        val filesToReferences = fileNames.map { fileName =>
          fileName -> (1 to referencesPerFile).map { i =>
            createReferenceTypeStub(
              name = "stub" + i,
              sourcePaths = Seq(fileName),
              locations = Nil
            )
          }
        }.toMap
        val stubReferenceTypes = filesToReferences.values.flatten.toSeq

        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify that we have the classes grouped by file name
        fileNames.foreach { fileName =>
          val expected = filesToReferences(fileName)
          val actual = classManager.underlyingReferencesForFile(fileName).get

          actual should contain theSameElementsAs (expected)
        }
      }

      it("should group arrays into the array group") {
        val referencesPerFile = 3
        val fileNames = Seq(
          "test1.scala",
          "test2.scala",
          "test3.scala"
        )

        // Create our classes with file names
        val filesToReferences = fileNames.map { fileName =>
          fileName -> (1 to referencesPerFile).map { i =>
            createReferenceTypeStub(
              name = "stub" + i,
              sourcePaths = Seq(fileName),
              locations = Nil
            )
          }
        }.toMap
        val arrayReferences = Seq(
          createReferenceTypeStub("array1[]", Seq("a", "b"), Nil),
          createReferenceTypeStub("array2[]", Seq("a", "b"), Nil)
        )
        val stubReferenceTypes =
          filesToReferences.values.flatten.toSeq ++ arrayReferences

        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify that we have the classes grouped by file name
        val expected = arrayReferences
        val actual = classManager.underlyingReferencesForFile(
          ClassManager.DefaultArrayGroupName
        ).get

        actual should contain theSameElementsAs expected
      }

      it("should group unknown classes (no file name and not array) into unknown") {
        val referencesPerFile = 3
        val fileNames = Seq(
          "test1.scala",
          "test2.scala",
          "test3.scala"
        )

        // Create our classes with file names
        val filesToReferences = fileNames.map { fileName =>
          fileName -> (1 to referencesPerFile).map { i =>
            createReferenceTypeStub(
              name = "stub" + i,
              sourcePaths = Seq(fileName),
              locations = Nil
            )
          }
        }.toMap
        val unknownReferences = Seq(
          createReferenceTypeStub("???1", Seq("a", "b"), Nil),
          createReferenceTypeStub("???2", Seq("a", "b"), Nil)
        )
        val stubReferenceTypes =
          filesToReferences.values.flatten.toSeq ++ unknownReferences

        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify that we have the classes grouped by file name
        val expected = unknownReferences
        val actual = classManager.underlyingReferencesForFile(
          ClassManager.DefaultUnknownGroupName
        ).get

        actual should contain theSameElementsAs expected
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

    describe("#fileNameForReferenceType") {
      it("should return the file name if the source paths are convergent") {
        val expected = "some source file"
        val mockReferenceType = mock[ReferenceType]
        (mockReferenceType.sourcePaths _).expects(*)
          .returning(Seq(expected).asJava).once()

        val actual = classManager.fileNameForReferenceType(mockReferenceType)

        actual should be (expected)
      }

      it("should return ARRAY if the source paths are divergent and is an array") {
        val expected = "ARRAY"
        val mockReferenceType = mock[ReferenceType]
        (mockReferenceType.sourcePaths _).expects(*)
          .returning(Seq("a", "b").asJava).once()
        (mockReferenceType.name _).expects().returning("somearray[]").once()

        val actual = classManager.fileNameForReferenceType(mockReferenceType)

        actual should be (expected)
      }

      it("should return UNKNOWN if the source paths are divergent and is not an array") {
        val expected = "UNKNOWN"
        val mockReferenceType = mock[ReferenceType]
        (mockReferenceType.sourcePaths _).expects(*)
          .returning(Seq("a", "b").asJava).once()
        (mockReferenceType.name _).expects().returning("someunknown").once()

        val actual = classManager.fileNameForReferenceType(mockReferenceType)

        actual should be (expected)
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

    describe("#allFileNames") {
      it("should return all file names") {
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
        classManager.allFileNames should
          contain theSameElementsAs fileNames
      }
    }

    describe("#allClasses") {
      it("should return a collection of all available reference types") {
        // Set up the virtual machine to return the classes
        (stubVirtualMachine.allClasses _).when()
          .returns(stubReferenceTypes.asJava)

        // Load the classes into the manager
        classManager.refreshAllClasses()

        // Verify we have the same reference types
        classManager.allClasses should
          contain theSameElementsAs stubReferenceTypes
      }
    }
  }
}
