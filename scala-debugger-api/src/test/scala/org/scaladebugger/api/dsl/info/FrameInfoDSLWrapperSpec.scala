package org.scaladebugger.api.dsl.info

import com.sun.jdi.StackFrame
import org.scaladebugger.api.profiles.traits.info._
import org.scaladebugger.api.virtualmachines.ScalaVirtualMachine
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

import scala.util.{Success, Try}

class FrameInfoDSLWrapperSpec extends test.ParallelMockFunSpec
{
  // NOTE: Cannot mock Function0 with no parentheses
  //private val mockFrameInfoProfile = mock[FrameInfo]
  
  private val testFrameInfoProfile = new TestFrameInfo

  describe("FrameInfoDSLWrapper") {
    describe("#withThisObject") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(mock[ObjectInfo])

        mockTryGetThisObjectFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withThisObject should be (returnValue)
      }
    }

    describe("#withUnsafeThisObject") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = mock[ObjectInfo]

        mockGetThisObjectFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withUnsafeThisObject should be (returnValue)
      }
    }

    describe("#withCurrentThread") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(mock[ThreadInfo])

        mockTryGetCurrentThreadFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withCurrentThread should be (returnValue)
      }
    }

    describe("#withUnsafeCurrentThread") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = mock[ThreadInfo]

        mockGetCurrentThreadFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.withUnsafeCurrentThread should be (returnValue)
      }
    }

    describe("#forVariable") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val name = "someName"
        val returnValue = Success(mock[VariableInfo])

        mockTryGetVariableFunction.expects(name)
          .returning(returnValue).once()

        testFrameInfoProfile.forVariable(name) should be (returnValue)
      }
    }

    describe("#forUnsafeVariable") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val name = "someName"
        val returnValue = mock[VariableInfo]

        mockGetVariableFunction.expects(name)
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeVariable(name) should be (returnValue)
      }
    }

    describe("#forAllVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[VariableInfo]))

        mockTryGetAllVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forAllVariables should be (returnValue)
      }
    }

    describe("#forUnsafeAllVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[VariableInfo])

        mockGetAllVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeAllVariables should be (returnValue)
      }
    }

    describe("#forArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfo]))

        mockTryGetArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forArguments should be (returnValue)
      }
    }

    describe("#forUnsafeArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfo])

        mockGetArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeArguments should be (returnValue)
      }
    }

    describe("#forNonArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfo]))

        mockTryGetNonArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forNonArguments should be (returnValue)
      }
    }

    describe("#forUnsafeNonArguments") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfo])

        mockGetNonArgumentsFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeNonArguments should be (returnValue)
      }
    }

    describe("#forLocalVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[IndexedVariableInfo]))

        mockTryGetLocalVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forLocalVariables should be (returnValue)
      }
    }

    describe("#forUnsafeLocalVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[IndexedVariableInfo])

        mockGetLocalVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeLocalVariables should be (returnValue)
      }
    }

    describe("#forFieldVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Success(Seq(mock[FieldVariableInfo]))

        mockTryGetFieldVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forFieldVariables should be (returnValue)
      }
    }

    describe("#forUnsafeFieldVariables") {
      it("should invoke the underlying profile method") {
        import org.scaladebugger.api.dsl.Implicits.FrameInfoDSL

        val returnValue = Seq(mock[FieldVariableInfo])

        mockGetFieldVariablesFunction.expects()
          .returning(returnValue).once()

        testFrameInfoProfile.forUnsafeFieldVariables should be (returnValue)
      }
    }
  }

  private val mockScalaVirtualMachineFunction = mockFunction[ScalaVirtualMachine]
  private val mockIndexFunction = mockFunction[Int]
  private val mockTryGetThisObjectFunction = mockFunction[Try[ObjectInfo]]
  private val mockGetThisObjectFunction = mockFunction[ObjectInfo]
  private val mockTryGetCurrentThreadFunction = mockFunction[Try[ThreadInfo]]
  private val mockGetCurrentThreadFunction = mockFunction[ThreadInfo]
  private val mockTryGetLocationFunction = mockFunction[Try[LocationInfo]]
  private val mockGetLocationFunction = mockFunction[LocationInfo]
  private val mockTryGetArgumentValuesFunction = mockFunction[Try[Seq[ValueInfo]]]
  private val mockGetArgumentValuesFunction = mockFunction[Seq[ValueInfo]]
  private val mockTryGetVariableFunction = mockFunction[String, Try[VariableInfo]]
  private val mockGetVariableFunction = mockFunction[String, VariableInfo]
  private val mockTryGetAllVariablesFunction = mockFunction[Try[Seq[VariableInfo]]]
  private val mockTryGetArgumentsFunction = mockFunction[Try[Seq[IndexedVariableInfo]]]
  private val mockTryGetNonArgumentsFunction = mockFunction[Try[Seq[IndexedVariableInfo]]]
  private val mockTryGetLocalVariablesFunction = mockFunction[Try[Seq[IndexedVariableInfo]]]
  private val mockTryGetFieldVariablesFunction = mockFunction[Try[Seq[FieldVariableInfo]]]
  private val mockGetAllVariablesFunction = mockFunction[Seq[VariableInfo]]
  private val mockGetArgumentsFunction = mockFunction[Seq[IndexedVariableInfo]]
  private val mockGetNonArgumentsFunction = mockFunction[Seq[IndexedVariableInfo]]
  private val mockGetLocalVariablesFunction = mockFunction[Seq[IndexedVariableInfo]]
  private val mockGetFieldVariablesFunction = mockFunction[Seq[FieldVariableInfo]]
  private val mockToJdiInstanceFunction = mockFunction[StackFrame]

  private class TestFrameInfo extends FrameInfo {
    override def toJavaInfo: FrameInfo = ???
    override def isJavaInfo: Boolean = ???
    override def thisObjectOption: Option[ObjectInfo] = ???
    override def variableOption(name: String): Option[VariableInfo] = ???
    override def indexedVariableOption(name: String): Option[VariableInfo] = ???
    override def indexedArgumentLocalVariables: Seq[IndexedVariableInfo] = ???
    override def indexedFieldVariables: Seq[FieldVariableInfo] = ???
    override def indexedAllVariables: Seq[VariableInfo] = ???
    override def indexedNonArgumentLocalVariables: Seq[IndexedVariableInfo] = ???
    override def indexedLocalVariables: Seq[IndexedVariableInfo] = ???
    override def scalaVirtualMachine: ScalaVirtualMachine = mockScalaVirtualMachineFunction()
    override def index: Int = mockIndexFunction()
    override def tryThisObject: Try[ObjectInfo] = mockTryGetThisObjectFunction()
    override def thisObject: ObjectInfo = mockGetThisObjectFunction()
    override def tryCurrentThread: Try[ThreadInfo] = mockTryGetCurrentThreadFunction()
    override def currentThread: ThreadInfo = mockGetCurrentThreadFunction()
    override def tryLocation: Try[LocationInfo] = mockTryGetLocationFunction()
    override def location: LocationInfo = mockGetLocationFunction()
    override def tryArgumentValues: Try[Seq[ValueInfo]] = mockTryGetArgumentValuesFunction()
    override def argumentValues: Seq[ValueInfo] = mockGetArgumentValuesFunction()
    override def tryVariable(name: String): Try[VariableInfo] = mockTryGetVariableFunction(name)
    override def variable(name: String): VariableInfo = mockGetVariableFunction(name)
    override def tryAllVariables: Try[Seq[VariableInfo]] = mockTryGetAllVariablesFunction()
    override def tryArgumentLocalVariables: Try[Seq[IndexedVariableInfo]] = mockTryGetArgumentsFunction()
    override def tryNonArgumentLocalVariables: Try[Seq[IndexedVariableInfo]] = mockTryGetNonArgumentsFunction()
    override def tryLocalVariables: Try[Seq[IndexedVariableInfo]] = mockTryGetLocalVariablesFunction()
    override def tryFieldVariables: Try[Seq[FieldVariableInfo]] = mockTryGetFieldVariablesFunction()
    override def allVariables: Seq[VariableInfo] = mockGetAllVariablesFunction()
    override def argumentLocalVariables: Seq[IndexedVariableInfo] = mockGetArgumentsFunction()
    override def nonArgumentLocalVariables: Seq[IndexedVariableInfo] = mockGetNonArgumentsFunction()
    override def localVariables: Seq[IndexedVariableInfo] = mockGetLocalVariablesFunction()
    override def fieldVariables: Seq[FieldVariableInfo] = mockGetFieldVariablesFunction()
    override def toJdiInstance: StackFrame = mockToJdiInstanceFunction()
  }
}
