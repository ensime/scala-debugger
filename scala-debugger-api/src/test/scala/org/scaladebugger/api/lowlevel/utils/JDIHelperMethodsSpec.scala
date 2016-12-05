package org.scaladebugger.api.lowlevel.utils

import com.sun.jdi._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

class JDIHelperMethodsSpec extends test.ParallelMockFunSpec
{
  private val mockReferenceType = mock[ReferenceType]
  private val mockThreadReference = mock[ThreadReference]
  private val mockVirtualMachine = mock[VirtualMachine]
  private class PublicJDIHelperMethods extends JDIHelperMethods {
    override protected val _virtualMachine: VirtualMachine = mockVirtualMachine

    override def suspendVirtualMachineAndExecute[T](thunk: => T): Try[T] =
      super.suspendVirtualMachineAndExecute(thunk)

    override def suspendThreadAndExecute[T](
      threadReference: ThreadReference
    )(thunk: => T): Try[T] =
      super.suspendThreadAndExecute(threadReference)(thunk)

    override def findMainThread(): Option[ThreadReference] =
      super.findMainThread()

    override def findMainThread(virtualMachine: VirtualMachine): Option[ThreadReference] =
      super.findMainThread(virtualMachine)

    override def singleSourcePath(referenceType: ReferenceType): Option[String] =
      super.singleSourcePath(referenceType)

    override def retrieveCommandLineArguments(): Seq[String] =
      super.retrieveCommandLineArguments()

    override def retrieveMainClassName(): String =
      super.retrieveMainClassName()
  }
  private val jdiHelperMethods = new PublicJDIHelperMethods

  describe("JDIHelperMethods") {
    describe("#suspendVirtualMachineAndExecute") {
      it("should suspend and then resume the virtual machine") {
        // Should attempt to suspend and then resume the virtual machine
        inSequence {
          (mockVirtualMachine.suspend _).expects().once()
          (mockVirtualMachine.resume _).expects().once()
        }

        jdiHelperMethods.suspendVirtualMachineAndExecute({})
      }

      it("should invoke the code while suspended and resume afterwards") {
        val mockCode = mockFunction[Unit]

        inSequence {
          (mockVirtualMachine.suspend _).expects().once()
          mockCode.expects().once()
          (mockVirtualMachine.resume _).expects().once()
        }

        jdiHelperMethods.suspendVirtualMachineAndExecute(mockCode())
      }

      it("should return the result of the code execution wrapped in a try") {
        val expected = 3
        val mockCode = mockFunction[Int]

        inSequence {
          (mockVirtualMachine.suspend _).expects().once()
          mockCode.expects().returning(expected).once()
          (mockVirtualMachine.resume _).expects().once()
        }

        val actual =
          jdiHelperMethods.suspendVirtualMachineAndExecute(mockCode())

        actual should be (Success(expected))
      }

      it("should wrap the exceptions in a Try when executing the code") {
        val expected = new Throwable
        val mockCode = mockFunction[Unit]

        inSequence {
          (mockVirtualMachine.suspend _).expects().once()
          mockCode.expects().throwing(expected).once()
          (mockVirtualMachine.resume _).expects().once()
        }

        val actual =
          jdiHelperMethods.suspendVirtualMachineAndExecute(mockCode())

        actual should be (Failure(expected))
      }
    }

    describe("#suspendThreadAndExecute") {
      it("should suspend and then resume the thread reference") {
        // Should attempt to suspend and then resume the virtual machine
        inSequence {
          (mockThreadReference.suspend _).expects().once()
          (mockThreadReference.resume _).expects().once()
        }

        jdiHelperMethods.suspendThreadAndExecute(mockThreadReference) {}
      }

      it("should invoke the code while suspended and resume afterwards") {
        val mockCode = mockFunction[Unit]

        inSequence {
          (mockThreadReference.suspend _).expects().once()
          mockCode.expects().once()
          (mockThreadReference.resume _).expects().once()
        }

        jdiHelperMethods.suspendThreadAndExecute(mockThreadReference) {
          mockCode()
        }
      }

      it("should return the result of the code execution wrapped in a try") {
        val expected = 3
        val mockCode = mockFunction[Int]

        inSequence {
          (mockThreadReference.suspend _).expects().once()
          mockCode.expects().returning(expected).once()
          (mockThreadReference.resume _).expects().once()
        }

        val actual =
          jdiHelperMethods.suspendThreadAndExecute(mockThreadReference) {
            mockCode()
          }

        actual should be (Success(expected))
      }

      it("should wrap the exceptions in a Try when executing the code") {
        val expected = new Throwable
        val mockCode = mockFunction[Unit]

        inSequence {
          (mockThreadReference.suspend _).expects().once()
          mockCode.expects().throwing(expected).once()
          (mockThreadReference.resume _).expects().once()
        }

        val actual =
          jdiHelperMethods.suspendThreadAndExecute(mockThreadReference) {
            mockCode()
          }

        actual should be (Failure(expected))
      }
    }

    describe("#findMainMethod") {
      it("should return Some(thread reference to the main thread)") {
        val mockMainThread = mock[ThreadReference]
        (mockMainThread.name _).expects().returning("main")

        val totalOtherThreads = 3
        val mockOtherThreads = (1 to totalOtherThreads).map { i =>
          val threadReference = mock[ThreadReference]
          (threadReference.name _).expects().returning(s"other$i")
          threadReference
        }

        (mockVirtualMachine.allThreads _).expects()
          .returning((mockOtherThreads :+ mockMainThread).asJava)

        jdiHelperMethods.findMainThread(mockVirtualMachine) should
          be (Some(mockMainThread))
      }

      it("should return None if no main thread can be found") {
        val totalOtherThreads = 3
        val mockOtherThreads = (1 to totalOtherThreads).map { i =>
          val threadReference = mock[ThreadReference]
          (threadReference.name _).expects().returning(s"other$i")
          threadReference
        }

        (mockVirtualMachine.allThreads _).expects()
          .returning(mockOtherThreads.asJava)

        jdiHelperMethods.findMainThread(mockVirtualMachine) should be (None)
      }

      it("should use the underlying virtual machine reference if none provided") {
        (mockVirtualMachine.allThreads _).expects()
          .returning((Nil: Seq[ThreadReference]).asJava)

        // Attempt to retrieve the main thread (using underlying vm)
        jdiHelperMethods.findMainThread()
      }
    }

    describe("#singleSourcePath") {
      it("should return a single source path if all source paths for the reference type are the same") {
        val expected = Some("a")
        val sourcePaths = Seq("a", "a")

        // Attempts to retrieve default stratum
        (mockVirtualMachine.getDefaultStratum _).expects()

        // Set source paths as convergent
        (mockReferenceType.sourcePaths _).expects(*)
          .returning(sourcePaths.asJava)

        val actual = jdiHelperMethods.singleSourcePath(mockReferenceType)

        actual should be (expected)
      }

      it("should return None if the sourcePaths throws AbsentInformationException") {
        val expected = None

        // Attempts to retrieve default stratum
        (mockVirtualMachine.getDefaultStratum _).expects()

        // Throw exception trying to access source information
        (mockReferenceType.sourcePaths _).expects(*)
          .throwing(new AbsentInformationException())

        val actual = jdiHelperMethods.singleSourcePath(mockReferenceType)

        actual should be (expected)
      }

      it("should return None if the reference type comes from multiple sources") {
        val expected = None
        val sourcePaths = Seq("a", "b")

        // Attempts to retrieve default stratum
        (mockVirtualMachine.getDefaultStratum _).expects()

        // Set source paths as divergent
        (mockReferenceType.sourcePaths _).expects(*)
          .returning(sourcePaths.asJava)

        val actual = jdiHelperMethods.singleSourcePath(mockReferenceType)

        actual should be (expected)
      }
    }

    describe("#retrieveMainClassName") {
      it("should throw an exception if unable to find a main method") {
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] = None
        }

        // Should fail to acquire the main class name
        intercept[NoSuchElementException] {
          jdiHelperMethods.retrieveMainClassName()
        }
      }

      it("should throw an exception if encountered while getting the name") {
        val expected = new Throwable

        val mockMainThreadReference = mock[ThreadReference]
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] =
            Some(mockMainThreadReference)
        }

        inSequence {
          (mockMainThreadReference.suspend _).expects().once()
          (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
            .expects().throwing(expected)
          (mockMainThreadReference.resume _).expects().once()
        }

        val actual =
          the [Throwable] thrownBy jdiHelperMethods.retrieveMainClassName()

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

        val mockMainThreadReference = mock[ThreadReference]
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] =
            Some(mockMainThreadReference)
        }

        // Set the main thread to return a stack frame with the main method
        (mockMainThreadReference.suspend _).expects().once()
        (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects().returning(Seq(mockStackFrame).asJava)
        (mockMainThreadReference.resume _).expects().once()

        // Should fail to find a location with a main method
        intercept[AssertionError] {
          jdiHelperMethods.retrieveMainClassName()
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

        val mockMainThreadReference = mock[ThreadReference]
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] =
            Some(mockMainThreadReference)
        }

        // Set the main thread to return a stack frame with the main method
        (mockMainThreadReference.suspend _).expects().once()
        (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects().returning(Seq(mockStackFrame).asJava)
        (mockMainThreadReference.resume _).expects().once()

        val actual = jdiHelperMethods.retrieveMainClassName()

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

        val mockMainThreadReference = mock[ThreadReference]
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] =
            Some(mockMainThreadReference)
        }

        // Set the main thread to return a stack frame with the main method
        (mockMainThreadReference.suspend _).expects().once()
        (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
          .expects().returning(mockStackFrames.asJava)
        (mockMainThreadReference.resume _).expects().once()

        val actual = jdiHelperMethods.retrieveMainClassName()

        actual should be (expected)
      }
    }

    describe("#commandLineArguments") {
      it("should throw an exception if unable to find a main method") {
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] = None
        }

        // Should fail to acquire the main class name
        intercept[NoSuchElementException] {
          jdiHelperMethods.retrieveCommandLineArguments()
        }
      }

      it("should throw an exception if encountered while getting the arguments") {
        val expected = new Throwable

        val mockMainThreadReference = mock[ThreadReference]
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] =
            Some(mockMainThreadReference)
        }

        inSequence {
          (mockMainThreadReference.suspend _).expects().once()
          (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
            .expects().throwing(expected)
          (mockMainThreadReference.resume _).expects().once()
        }

        val actual = the [Throwable] thrownBy
          jdiHelperMethods.retrieveCommandLineArguments()

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

        val mockMainThreadReference = mock[ThreadReference]
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] =
            Some(mockMainThreadReference)
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
          jdiHelperMethods.retrieveCommandLineArguments()
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

        val mockMainThreadReference = mock[ThreadReference]
        val jdiHelperMethods = new PublicJDIHelperMethods {
          override def findMainThread(): Option[ThreadReference] =
            Some(mockMainThreadReference)
        }

        // Set the main thread to return a stack frame with the main method
        inSequence {
          (mockMainThreadReference.suspend _).expects().once()
          (mockMainThreadReference.frames: Function0[java.util.List[StackFrame]])
            .expects().returning(Seq(mockStackFrame).asJava)
          (mockMainThreadReference.resume _).expects().once()
        }

        val actual = jdiHelperMethods.retrieveCommandLineArguments()

        actual should contain theSameElementsInOrderAs expected
      }
    }
  }
}
