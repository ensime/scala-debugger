package org.senkbeil.debugger.api.virtualmachines

import org.senkbeil.debugger.api.classes.ClassManager
import org.senkbeil.debugger.api.events.LoopingTaskRunner
import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import test.JDIMockHelpers

import scala.collection.JavaConverters._

class ScalaVirtualMachineSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory with JDIMockHelpers
{
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]
  private val mockMainThreadReference = mock[ThreadReference]

  // NOTE: Needed until https://github.com/paulbutcher/ScalaMock/issues/56
  class ZeroArgClassManager
    extends ClassManager(mockVirtualMachine, loadClasses = false)
  private val mockClassManager = mock[ZeroArgClassManager]

  // Scala virtual machine sets up event requests on construction
  private val scalaVirtualMachine = new ScalaVirtualMachine(
    mockVirtualMachine,
    mockLoopingTaskRunner
  ) {
    override protected def initializeEvents(): Unit = {}
    override lazy val classManager: ClassManager = mockClassManager
    override protected def findMainThread(): Option[ThreadReference] =
      Some(mockMainThreadReference)
  }

  describe("ScalaVirtualMachine") {
    describe("#availableLinesForFile") {
      it("should return the lines (sorted) that can have breakpoints") {
        val expected = Seq(1, 8, 999)

        // Setup the return from class manager to be reverse order
        val linesAndLocations = expected.reverseMap(i =>
          (i, Seq(stub[Location]))
        ).toMap
        (mockClassManager.linesAndLocationsForFile _).expects(*)
          .returning(Some(linesAndLocations))

        val actual = scalaVirtualMachine.availableLinesForFile("").get

        actual should contain theSameElementsInOrderAs expected
      }

      it("should return None if the file does not exist") {
        val expected = None

        // Set the return from class manager to be "not found"
        (mockClassManager.linesAndLocationsForFile _).expects(*).returning(None)

        val actual = scalaVirtualMachine.availableLinesForFile("")

        actual should be (expected)
      }
    }

    describe("#mainClassName") {
      it("should throw an exception if unable to find a main method") {
        val scalaVirtualMachine = new ScalaVirtualMachine(
          mockVirtualMachine,
          mockLoopingTaskRunner
        ) {
          override protected def initializeEvents(): Unit = {}
          override lazy val classManager: ClassManager = mockClassManager
          override protected def findMainThread(): Option[ThreadReference] =
            None
        }

        // Should fail to acquire the main class name
        intercept[NoSuchElementException] {
          scalaVirtualMachine.mainClassName
        }
      }

      it("should throw an exception if encountered while getting the name") {
        val expected = new Throwable

        inSequence {
          (mockMainThreadReference.suspend _).expects().once()
          (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
            .expects().throwing(expected)
          (mockMainThreadReference.resume _).expects().once()
        }

        val actual = the [Throwable] thrownBy scalaVirtualMachine.mainClassName

        actual should be (expected)
      }

      it("should throw an assertion exception (caught by Try) if could not find main method") {
        // Create a stack frame whose location returns the expected main
        // method name
        val mockStackFrame = {
          val _stackFrame = mock[StackFrame]
          (_stackFrame.location _).expects().returning({
            val _location = mock[Location]

            // Set the method to be "not main" (so should not find a method)
            (_location.method _).expects().returning({
              val _method = mock[Method]
              (_method.name _).expects().returning("not main")
              _method
            })

            _location
          })
          _stackFrame
        }

        // Set the main thread to return a stack frame with the main method
        (mockMainThreadReference.suspend _).expects().once()
        (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects().returning(Seq(mockStackFrame).asJava)
        (mockMainThreadReference.resume _).expects().once()

        // Should fail to find a location with a main method
        intercept[AssertionError] {
          scalaVirtualMachine.mainClassName
        }
      }

      it("should return the name of the class containing the main method") {
        val expected = "some awesome class"

        // Create a stack frame whose location returns the expected main
        // method name
        val mockStackFrame = {
          val _stackFrame = mock[StackFrame]
          (_stackFrame.location _).expects().returning({
            val _location = mock[Location]

            // Set the method to be "main"
            (_location.method _).expects().returning({
              val _method = mock[Method]
              (_method.name _).expects().returning("main")
              _method
            })

            // Set the returning class name to be the expected
            (_location.declaringType _).expects().returning({
              val _referenceType = mock[ReferenceType]
              (_referenceType.name _).expects().returning(expected)
              _referenceType
            })
            _location
          })
          _stackFrame
        }

        // Set the main thread to return a stack frame with the main method
        (mockMainThreadReference.suspend _).expects().once()
        (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects().returning(Seq(mockStackFrame).asJava)
        (mockMainThreadReference.resume _).expects().once()

        val actual = scalaVirtualMachine.mainClassName

        actual should be (expected)
      }

      it("should return the name of the base class containing the main method") {
        val expected = "MyBaseClass"

        // Create a stack frame whose location returns the expected main
        // method name
        def setupMockStackFrame(locationDeclaringTypeName: String) = {
          val _stackFrame = mock[StackFrame]
          (_stackFrame.location _).expects().returning({
            val _location = mock[Location]

            // Set the method to be "main"
            (_location.method _).expects().returning({
              val _method = mock[Method]
              (_method.name _).expects().returning("main")
              _method
            })

            // Set the returning class name to be the expected
            (_location.declaringType _).expects().returning({
              val _referenceType = mock[ReferenceType]
              (_referenceType.name _).expects()
                .returning(expected).atLeastOnce()
              _referenceType
            }).atLeastOnce()
            _location
          })
          _stackFrame
        }

        val mockStackFrames = Seq(
          setupMockStackFrame(expected + "$"),
          setupMockStackFrame(expected),
          setupMockStackFrame(expected + "$InnerClass")
        )

        // Set the main thread to return a stack frame with the main method
        (mockMainThreadReference.suspend _).expects().once()
        (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects().returning(mockStackFrames.asJava)
        (mockMainThreadReference.resume _).expects().once()

        val actual = scalaVirtualMachine.mainClassName

        actual should be (expected)
      }
    }

    describe("#commandLineArguments") {
      it("should throw an exception if unable to find a main method") {
        val scalaVirtualMachine = new ScalaVirtualMachine(
          mockVirtualMachine,
          mockLoopingTaskRunner
        ) {
          override protected def initializeEvents(): Unit = {}
          override lazy val classManager: ClassManager = mockClassManager
          override protected def findMainThread(): Option[ThreadReference] =
            None
        }

        // Should fail to acquire the main class name
        intercept[NoSuchElementException] {
          scalaVirtualMachine.commandLineArguments
        }
      }

      it("should throw an exception if encountered while getting the arguments") {
        val expected = new Throwable

        inSequence {
          (mockMainThreadReference.suspend _).expects().once()
          (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
            .expects().throwing(expected)
          (mockMainThreadReference.resume _).expects().once()
        }

        val actual = the [Throwable] thrownBy
          scalaVirtualMachine.commandLineArguments

        actual should be (expected)
      }

      it("should throw an assertion exception (caught by Try) if could not find main method") {
        // Create a stack frame whose location does not return the expected
        // main method name
        val mockStackFrame = {
          val _stackFrame = mock[StackFrame]
          (_stackFrame.location _).expects().returning({
            val _location = mock[Location]

            // Set the method to be "not main" (so should not find a method)
            (_location.method _).expects().returning({
              val _method = mock[Method]
              (_method.name _).expects().returning("not main")
              _method
            })

            _location
          })
          _stackFrame
        }

        // Set the main thread to return a stack frame with the main method
        inSequence {
          (mockMainThreadReference.suspend _).expects().once()
          (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
            .expects().returning(Seq(mockStackFrame).asJava)
          (mockMainThreadReference.resume _).expects().once()
        }

        // Should fail to find a location with a main method
        intercept[AssertionError] {
          scalaVirtualMachine.commandLineArguments
        }
      }

      it("should return a list of string arguments provided to the JVM") {
        val expected = Seq("some", "series", "-f", "of", "arguments")

        // Create a stack frame whose location does return the expected
        // main method name
        val mockStackFrame = {
          val _stackFrame = mock[StackFrame]
          (_stackFrame.location _).expects().returning({
            val _location = mock[Location]

            // Set the method to be "not main" (so should not find a method)
            (_location.method _).expects().returning({
              val _method = mock[Method]
              (_method.name _).expects().returning("main")
              _method
            })

            _location
          })

          // Return a single array reference type
          (_stackFrame.getArgumentValues _).expects().returning(Seq({
            val _arrayReference = mock[ArrayReference]
            val _firstElement: Value = mock[ObjectReference]

            // Make the array contain an object reference in front of the
            // strings (since we are filtering that out)
            (_arrayReference.getValues: Function0[java.util.List[Value]])
              .expects().returning((_firstElement +: expected.map{ rawString =>
                val _stringReference = mock[StringReference]

                (_stringReference.value _).expects().returning(rawString)

                _stringReference
              }).asJava)

            _arrayReference
          }: Value).asJava)

          _stackFrame
        }

        // Set the main thread to return a stack frame with the main method
        inSequence {
          (mockMainThreadReference.suspend _).expects().once()
          (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
            .expects().returning(Seq(mockStackFrame).asJava)
          (mockMainThreadReference.resume _).expects().once()
        }

        val actual = scalaVirtualMachine.commandLineArguments

        actual should contain theSameElementsInOrderAs expected
      }
    }
  }
}
