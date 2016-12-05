package org.scaladebugger.api.debuggers
import com.sun.jdi.connect.{AttachingConnector, Connector}
import com.sun.jdi.{VirtualMachine, VirtualMachineManager}
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.utils.LoopingTaskRunner
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, ScalaVirtualMachineManager, StandardScalaVirtualMachine}

import scala.collection.JavaConverters._

class ProcessDebuggerSpec extends test.ParallelMockFunSpec {
  private def createConnectorArgumentMock(
    setter: Boolean = false,
    getter: Option[String] = None
  ) = {
    val mockConnectorArgument = mock[Connector.Argument]
    if (getter.nonEmpty)
      (mockConnectorArgument.value _).expects().returning(getter.get).once()
    if (setter) (mockConnectorArgument.setValue _).expects(*).once()
    mockConnectorArgument
  }

  private val testPid = 1234
  private val testTimeout = 9876

  private val mockVirtualMachineManager = mock[VirtualMachineManager]
  private val testScalaVirtualMachineManager = ScalaVirtualMachineManager.Instance
  private val mockVirtualMachine = mock[VirtualMachine]
  private val mockProfileManager = mock[ProfileManager]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]
  private val mockAddNewScalaVirtualMachineFunc = mockFunction[
    ScalaVirtualMachineManager, VirtualMachine, ProfileManager,
    LoopingTaskRunner, StandardScalaVirtualMachine
  ]

  private class TestScalaVirtualMachine extends StandardScalaVirtualMachine(
    testScalaVirtualMachineManager,
    mockVirtualMachine,
    mockProfileManager,
    mockLoopingTaskRunner
  )
  private val mockScalaVirtualMachine = mock[TestScalaVirtualMachine]

  private class TestProcessDebugger(
    override val isAvailable: Boolean = true,
    private val shouldJdiLoad: Boolean = true,
    private val customScalaVirtualMachine: Option[StandardScalaVirtualMachine] = Some(null)
  ) extends ProcessDebugger(
    virtualMachineManager = mockVirtualMachineManager,
    profileManager        = mockProfileManager,
    loopingTaskRunner     = mockLoopingTaskRunner,
    pid                   = testPid,
    timeout               = testTimeout
  ) {
    override protected def addNewScalaVirtualMachine(
      scalaVirtualMachineManager: ScalaVirtualMachineManager,
      virtualMachine: VirtualMachine,
      profileManager: ProfileManager,
      loopingTaskRunner: LoopingTaskRunner
    ): ScalaVirtualMachine = mockAddNewScalaVirtualMachineFunc(
      scalaVirtualMachineManager,
      virtualMachine,
      profileManager,
      loopingTaskRunner
    )

    override def assertJdiLoaded(): Unit =
      if (!shouldJdiLoad) throw new AssertionError
  }

  describe("ProcessDebugger") {
    describe("#start") {
      it("should throw an exception if unable to load JDI") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = false)

        intercept[AssertionError] {
          processDebugger.start((_) => {})
        }
      }

      it("should throw an exception if already started") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockAddNewScalaVirtualMachineFunc.expects(*, *, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        processDebugger.start((_) => {})

        intercept[AssertionError] {
          processDebugger.start((_) => {})
        }
      }

      it("should throw an exception if unable to get the process connector") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)
        (mockVirtualMachineManager.allConnectors _).expects()
          .returning((Nil: Seq[Connector]).asJava)

        intercept[AssertionError] {
          processDebugger.start((_) => {})
        }
      }

      it("should invoke the callback function with the new virtual machine") {
        val expected = stub[TestScalaVirtualMachine]
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockAddNewScalaVirtualMachineFunc.expects(*, *, *, *)
          .returning(expected).once()
        // MOCK ===============================================================

        var actual: ScalaVirtualMachine = null
        processDebugger.start(actual = _)

        actual should be (expected)
      }

      it("should apply any pending requests to the virtual machine") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)
        val expected = stub[TestScalaVirtualMachine]
        processDebugger.addPendingScalaVirtualMachine(expected)

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        // MOCK ===============================================================

        mockAddNewScalaVirtualMachineFunc.expects(
          testScalaVirtualMachineManager, mockVirtualMachine, *, *
        ).returning(mockScalaVirtualMachine).once()

        (mockScalaVirtualMachine.processPendingRequests _)
          .expects(expected).once()

        (mockScalaVirtualMachine.initialize _)
          .expects(Debugger.DefaultProfileName, true).once()

        processDebugger.start(_ => {})
      }

      it("should initialize the new virtual machine") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        // MOCK ===============================================================

        mockAddNewScalaVirtualMachineFunc.expects(
          testScalaVirtualMachineManager, mockVirtualMachine, *, *
        ).returning(mockScalaVirtualMachine).once()

        (mockScalaVirtualMachine.initialize _)
          .expects(Debugger.DefaultProfileName, true).once()

        processDebugger.start(_ => {})
      }
    }

    describe("#isRunning") {
      it("should return true if already started") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockAddNewScalaVirtualMachineFunc.expects(*, *, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        processDebugger.start(_ => {})

        processDebugger.isRunning should be (true)
      }

      it("should return false if not started (or started and then stopped)") {
        val processDebugger = new TestProcessDebugger()
        processDebugger.isRunning should be (false)
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        val processDebugger = new TestProcessDebugger()

        intercept[AssertionError] {
          processDebugger.stop()
        }
      }

      it("should dispose of the virtual machine mirror") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockAddNewScalaVirtualMachineFunc.expects(*, *, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        processDebugger.start((_) => {})

        (mockLoopingTaskRunner.stop _).expects(true).once()
        (mockVirtualMachine.dispose _).expects().once()

        processDebugger.stop()
      }
    }

    describe("#connectedScalaVirtualMachines") {
      it("should return an empty list if the debugger has not connected") {
        val processDebugger = new TestProcessDebugger()

        processDebugger.connectedScalaVirtualMachines should be (empty)
      }

      it("should return a list with one virtual machine when connected") {
        val processDebugger = new TestProcessDebugger()
        val stubScalaVirtualMachine = stub[TestScalaVirtualMachine]

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockAddNewScalaVirtualMachineFunc.expects(*, *, *, *)
          .returning(stubScalaVirtualMachine).once()
        // MOCK ===============================================================

        processDebugger.start((_) => {})

        processDebugger.connectedScalaVirtualMachines should
          contain (stubScalaVirtualMachine)
      }

      it("should return an empty list if stopped after a virtual machine has connected") {
        val processDebugger = new TestProcessDebugger()

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockAddNewScalaVirtualMachineFunc.expects(*, *, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        (mockLoopingTaskRunner.stop _).expects(true).once()
        (mockVirtualMachine.dispose _).expects().once()
        // MOCK ===============================================================

        processDebugger.start((_) => {})
        processDebugger.stop()

        processDebugger.connectedScalaVirtualMachines should be (empty)
      }
    }

    describe("#process") {
      it("should return Some process if the JVM has been attached") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockAttachingConnector = mock[AttachingConnector]

        (mockAttachingConnector.name _).expects()
          .returning("com.sun.jdi.ProcessAttach")

        (mockVirtualMachineManager.allConnectors _).expects()
          .returning(Seq(mockAttachingConnector: Connector).asJava)

        (mockAttachingConnector.defaultArguments _).expects().returning(Map(
          "pid" -> createConnectorArgumentMock(setter = true),
          "timeout" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockAttachingConnector.attach _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockAddNewScalaVirtualMachineFunc.expects(*, *, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        val mockProcess = mock[Process]
        (mockVirtualMachine.process _).expects().returning(mockProcess).once()

        processDebugger.start((_) => {})

        processDebugger.process should be (Some(mockProcess))
      }

      it("should return None if the JVM has not been attached") {
        val processDebugger = new TestProcessDebugger(shouldJdiLoad = true)

        processDebugger.process should be (None)
      }
    }
  }
}
