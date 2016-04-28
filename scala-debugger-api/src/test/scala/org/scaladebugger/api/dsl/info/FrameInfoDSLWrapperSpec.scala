package org.scaladebugger.api.dsl.info

import com.sun.jdi.StackFrame
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.{Success, Try}

class FrameInfoDSLWrapperSpec extends FunSpec with Matchers
  with ParallelTestExecution with MockFactory
{
  // NOTE: Cannot mock Function0 with no parentheses
  //private val mockFrameInfoProfile = mock[FrameInfoProfile]
  
  private val testFrameInfoProfile = new TestFrameInfoProfile

  describe("FrameInfoDSLWrapper") {
    describe("#withThisObject") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(mock[ObjectInfoProfile])

        mockTryGetThisObjectFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withThisObject should be (returnValue)
      }
    }

    describe("#withUnsafeThisObject") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = mock[ObjectInfoProfile]

        mockGetThisObjectFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withUnsafeThisObject should be (returnValue)
      }
    }

    describe("#withCurrentThread") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(mock[ThreadInfoProfile])

        mockTryGetCurrentThreadFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withCurrentThread should be (returnValue)
      }
    }

    describe("#withUnsafeCurrentThread") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = mock[ThreadInfoProfile]

        mockGetCurrentThreadFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withUnsafeCurrentThread should be (returnValue)
      }
    }

    describe("#forVariable") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val name = "someName"
        val returnValue = Success(mock[VariableInfoProfile])

        mockTryGetVariableFunction.expects(name)
          .returning(returnValue).once()

        testFrameInfoProfile.forVariable(name) should be (returnValue)
      }
    }

    describe("#forUnsafeVariable") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val name = "someName"
        val returnValue = mock[VariableInfoProfile]

        mockGetVariableFunction.expects(name)
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeVariable(name) should be (returnValue)
      }
    }

    describe("#forAllVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[VariableInfoProfile]))

        mockTryGetAllVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forAllVariables should be (returnValue)
      }
    }

    describe("#forUnsafeAllVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[VariableInfoProfile])

        mockGetAllVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeAllVariables should be (returnValue)
      }
    }

    describe("#forArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfoProfile]))

        mockTryGetArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forArguments should be (returnValue)
      }
    }

    describe("#forUnsafeArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfoProfile])

        mockGetArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeArguments should be (returnValue)
      }
    }

    describe("#forNonArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfoProfile]))

        mockTryGetNonArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forNonArguments should be (returnValue)
      }
    }

    describe("#forUnsafeNonArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfoProfile])

        mockGetNonArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeNonArguments should be (returnValue)
      }
    }

    describe("#forLocalVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfoProfile]))

        mockTryGetLocalVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forLocalVariables should be (returnValue)
      }
    }

    describe("#forUnsafeLocalVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfoProfile])

        mockGetLocalVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeLocalVariables should be (returnValue)
      }
    }

    describe("#forFieldVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[VariableInfoProfile]))

        mockTryGetFieldVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forFieldVariables should be (returnValue)
      }
    }

    describe("#forUnsafeFieldVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[VariableInfoProfile])

        mockGetFieldVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeFieldVariables should be (returnValue)
      }
    }
  }

  private val mockScalaVirtualMachineFunction = mockFunction[ScalaVirtualMachine]
  private val mockIndexFunction = mockFunction[Int]
  private val mockTryGetThisObjectFunction = mockFunction[Try[ObjectInfoProfile]]
  private val mockGetThisObjectFunction = mockFunction[ObjectInfoProfile]
  private val mockTryGetCurrentThreadFunction = mockFunction[Try[ThreadInfoProfile]]
  private val mockGetCurrentThreadFunction = mockFunction[ThreadInfoProfile]
  private val mockTryGetLocationFunction = mockFunction[Try[LocationInfoProfile]]
  private val mockGetLocationFunction = mockFunction[LocationInfoProfile]
  private val mockTryGetArgumentValuesFunction = mockFunction[Try[Seq[ValueInfoProfile]]]
  private val mockGetArgumentValuesFunction = mockFunction[Seq[ValueInfoProfile]]
  private val mockTryGetVariableFunction = mockFunction[String, Try[VariableInfoProfile]]
  private val mockGetVariableFunction = mockFunction[String, VariableInfoProfile]
  private val mockTryGetAllVariablesFunction = mockFunction[Try[Seq[VariableInfoProfile]]]
  private val mockTryGetArgumentsFunction = mockFunction[Try[Seq[IndexedVariableInfoProfile]]]
  private val mockTryGetNonArgumentsFunction = mockFunction[Try[Seq[IndexedVariableInfoProfile]]]
  private val mockTryGetLocalVariablesFunction = mockFunction[Try[Seq[IndexedVariableInfoProfile]]]
  private val mockTryGetFieldVariablesFunction = mockFunction[Try[Seq[VariableInfoProfile]]]
  private val mockGetAllVariablesFunction = mockFunction[Seq[VariableInfoProfile]]
  private val mockGetArgumentsFunction = mockFunction[Seq[IndexedVariableInfoProfile]]
  private val mockGetNonArgumentsFunction = mockFunction[Seq[IndexedVariableInfoProfile]]
  private val mockGetLocalVariablesFunction = mockFunction[Seq[IndexedVariableInfoProfile]]
  private val mockGetFieldVariablesFunction = mockFunction[Seq[VariableInfoProfile]]
  private val mockToJdiInstanceFunction = mockFunction[StackFrame]

  private class TestFrameInfoProfile extends FrameInfoProfile {
    override def thisObjectOption: Option[ObjectInfoProfile] = ???
    override def variableOption(name: String): Option[VariableInfoProfile] = ???
    override def indexedVariableOption(name: String): Option[VariableInfoProfile] = ???
    override def indexedArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = ???
    override def indexedFieldVariables: Seq[VariableInfoProfile] = ???
    override def indexedAllVariables: Seq[VariableInfoProfile] = ???
    override def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = ???
    override def indexedLocalVariables: Seq[IndexedVariableInfoProfile] = ???
    override def scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachineFunction()
    override def index: Int = mockIndexFunction()
    override def tryThisObject: Try[ObjectInfoProfile] = mockTryGetThisObjectFunction()
    override def thisObject: ObjectInfoProfile = mockGetThisObjectFunction()
    override def tryCurrentThread: Try[ThreadInfoProfile] = mockTryGetCurrentThreadFunction()
    override def currentThread: ThreadInfoProfile = mockGetCurrentThreadFunction()
    override def tryLocation: Try[LocationInfoProfile] = mockTryGetLocationFunction()
    override def location: LocationInfoProfile = mockGetLocationFunction()
    override def tryArgumentValues: Try[Seq[ValueInfoProfile]] = mockTryGetArgumentValuesFunction()
    override def argumentValues: Seq[ValueInfoProfile] = mockGetArgumentValuesFunction()
    override def tryVariable(name: String): Try[VariableInfoProfile] = mockTryGetVariableFunction(name)
    override def variable(name: String): VariableInfoProfile = mockGetVariableFunction(name)
    override def tryAllVariables: Try[Seq[VariableInfoProfile]] = mockTryGetAllVariablesFunction()
    override def tryArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] = mockTryGetArgumentsFunction()
    override def tryNonArgumentLocalVariables: Try[Seq[IndexedVariableInfoProfile]] = mockTryGetNonArgumentsFunction()
    override def tryLocalVariables: Try[Seq[IndexedVariableInfoProfile]] = mockTryGetLocalVariablesFunction()
    override def tryFieldVariables: Try[Seq[VariableInfoProfile]] = mockTryGetFieldVariablesFunction()
    override def allVariables: Seq[VariableInfoProfile] = mockGetAllVariablesFunction()
    override def argumentLocalVariables: Seq[IndexedVariableInfoProfile] = mockGetArgumentsFunction()
    override def nonArgumentLocalVariables: Seq[IndexedVariableInfoProfile] = mockGetNonArgumentsFunction()
    override def localVariables: Seq[IndexedVariableInfoProfile] = mockGetLocalVariablesFunction()
    override def fieldVariables: Seq[VariableInfoProfile] = mockGetFieldVariablesFunction()
    override def toJdiInstance: StackFrame = mockToJdiInstanceFunction()
  }
}
