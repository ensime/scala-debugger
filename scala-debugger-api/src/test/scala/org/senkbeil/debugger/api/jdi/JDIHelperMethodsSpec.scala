package org.senkbeil.debugger.api.jdi

import com.sun.jdi.{AbsentInformationException, ThreadReference, ReferenceType, VirtualMachine}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, OneInstancePerTest}
import scala.collection.JavaConverters._

import scala.util.{Failure, Success, Try}

class JDIHelperMethodsSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
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
  }
}
