package org.scaladebugger.api.debuggers
import acyclic.file

import com.sun.jdi.connect.{Connector, LaunchingConnector}
import com.sun.jdi.{ReferenceType, VirtualMachine, VirtualMachineManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.utils.LoopingTaskRunner
import org.scaladebugger.api.virtualmachines.{ScalaVirtualMachine, StandardScalaVirtualMachine}

import scala.collection.JavaConverters._

class LaunchingDebuggerSpec extends test.ParallelMockFunSpec
{
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

  private val testClassName = "some class"
  private val testCommandLineArguments = Seq("a", "b", "c")
  private val testJvmOptions = Seq("d", "e", "f")
  private val testSuspend = false

  private val mockVirtualMachineManager = mock[VirtualMachineManager]
  private val mockProfileManager = mock[ProfileManager]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]
  private val mockNewScalaVirtualMachineFunc = mockFunction[
    VirtualMachine, ProfileManager, LoopingTaskRunner, StandardScalaVirtualMachine
  ]

  private class TestScalaVirtualMachine extends StandardScalaVirtualMachine(
    mockVirtualMachine, mockProfileManager, mockLoopingTaskRunner
  )
  private val mockScalaVirtualMachine = mock[TestScalaVirtualMachine]

  private class TestLaunchingDebugger(
    override val isAvailable: Boolean = true,
    private val shouldJdiLoad: Boolean = true
  ) extends LaunchingDebugger(
    virtualMachineManager = mockVirtualMachineManager,
    profileManager        = mockProfileManager,
    loopingTaskRunner     = mockLoopingTaskRunner,
    className             = testClassName,
    commandLineArguments  = testCommandLineArguments,
    jvmOptions            = testJvmOptions,
    suspend               = testSuspend
  ) {
    override protected def newScalaVirtualMachine(
      virtualMachine: VirtualMachine,
      profileManager: ProfileManager,
      loopingTaskRunner: LoopingTaskRunner
    ): StandardScalaVirtualMachine = mockNewScalaVirtualMachineFunc(
      virtualMachine,
      profileManager,
      loopingTaskRunner
    )

    override def assertJdiLoaded(): Unit =
      if (!shouldJdiLoad) throw new AssertionError
  }

  private val mockVirtualMachine = mock[VirtualMachine]

  describe("LaunchingDebugger") {
    describe("#start") {
      it("should throw an exception if unable to load JDI") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = false)

        intercept[AssertionError] {
          launchingDebugger.start((_) => {})
        }
      }

      it("should throw an exception if already started") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockNewScalaVirtualMachineFunc.expects(*, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        launchingDebugger.start((_) => {})

        intercept[AssertionError] {
          launchingDebugger.start((_) => {})
        }
      }

      it("should throw an exception if unable to get the launching connector") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)
        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning((Nil: Seq[LaunchingConnector]).asJava)

        intercept[AssertionError] {
          launchingDebugger.start((_) => {})
        }
      }

      it("should invoke the callback function with the new virtual machine") {
        val expected = stub[TestScalaVirtualMachine]
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()

        mockNewScalaVirtualMachineFunc.expects(*, *, *)
          .returning(expected).once()
        // MOCK ===============================================================

        var actual: ScalaVirtualMachine = null
        launchingDebugger.start(actual = _)

        actual should be (expected)
      }

      it("should apply any pending requests to the virtual machine") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)
        val expected = stub[TestScalaVirtualMachine]
        launchingDebugger.addPendingScalaVirtualMachine(expected)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        // MOCK ===============================================================

        mockNewScalaVirtualMachineFunc.expects(mockVirtualMachine, *, *)
          .returning(mockScalaVirtualMachine).once()

        (mockScalaVirtualMachine.processPendingRequests _)
          .expects(expected).once()

        (mockScalaVirtualMachine.initialize _)
          .expects(Debugger.DefaultProfileName, true).once()

        launchingDebugger.start(_ => {})
      }

      it("should initialize the new virtual machine") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        // MOCK ===============================================================

        mockNewScalaVirtualMachineFunc.expects(mockVirtualMachine, *, *)
          .returning(mockScalaVirtualMachine).once()

        (mockScalaVirtualMachine.initialize _)
          .expects(Debugger.DefaultProfileName, true).once()

        launchingDebugger.start(_ => {})
      }
    }

    describe("#isRunning") {
      it("should return true if already started") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockNewScalaVirtualMachineFunc.expects(*, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        launchingDebugger.start(_ => {})

        launchingDebugger.isRunning should be (true)
      }

      it("should return false if not started (or started and then stopped)") {
        val launchingDebugger = new TestLaunchingDebugger()
        launchingDebugger.isRunning should be (false)
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        val launchingDebugger = new TestLaunchingDebugger()

        intercept[AssertionError] {
          launchingDebugger.stop()
        }
      }

      // TODO: Add back the dispose of vm check when we figure out why dispose
      //       is throwing a VMDisconnectedException for the launching debugger
      it("should stop the running JVM process") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockNewScalaVirtualMachineFunc.expects(*, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        launchingDebugger.start((_) => {})

        (mockLoopingTaskRunner.stop _).expects(true).once()
        val mockProcess = mock[Process]
        (mockProcess.destroy _).expects().once()
        (mockVirtualMachine.process _).expects().returning(mockProcess).once()

        // TODO: Re-enable when determine why dispose throws exception
        //(mockVirtualMachine.dispose _).expects().once()

        launchingDebugger.stop()
      }
    }

    describe("#connectedScalaVirtualMachines") {
      it("should return an empty list if the debugger has not connected") {
        val launchingDebugger = new TestLaunchingDebugger()

        launchingDebugger.connectedScalaVirtualMachines should be (empty)
      }

      it("should return a list with one virtual machine when connected") {
        val launchingDebugger = new TestLaunchingDebugger()
        val stubScalaVirtualMachine = stub[TestScalaVirtualMachine]

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()

        mockNewScalaVirtualMachineFunc.expects(*, *, *)
          .returning(stubScalaVirtualMachine).once()
        // MOCK ===============================================================

        launchingDebugger.start(_ => {})

        launchingDebugger.connectedScalaVirtualMachines should
          contain (stubScalaVirtualMachine)
      }

      it("should return an empty list if stopped after a virtual machine has connected") {
        val launchingDebugger = new TestLaunchingDebugger()
        val stubScalaVirtualMachine = stub[TestScalaVirtualMachine]

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()

        mockNewScalaVirtualMachineFunc.expects(*, *, *)
          .returning(stubScalaVirtualMachine).once()

        (mockLoopingTaskRunner.stop _).expects(true).once()
        val mockProcess = mock[Process]
        (mockProcess.destroy _).expects().once()
        (mockVirtualMachine.process _).expects().returning(mockProcess).once()

        // TODO: Re-enable when determine why dispose throws exception
        //(mockVirtualMachine.dispose _).expects().once()
        // MOCK ===============================================================

        launchingDebugger.start(_ => {})
        launchingDebugger.stop()

        launchingDebugger.connectedScalaVirtualMachines should be (empty)
      }
    }

    describe("#process") {
      it("should return Some process if the JVM has been launched") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        // MOCK ===============================================================
        val mockLaunchingConnector = mock[LaunchingConnector]

        (mockLaunchingConnector.name _).expects()
          .returning("com.sun.jdi.CommandLineLaunch")

        (mockVirtualMachineManager.launchingConnectors _).expects()
          .returning(Seq(mockLaunchingConnector).asJava)

        (mockLaunchingConnector.defaultArguments _).expects().returning(Map(
          "main" -> createConnectorArgumentMock(setter = true),
          "options" -> createConnectorArgumentMock(
            setter = true, getter = Some("")
          ),
          "suspend" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockLaunchingConnector.launch _).expects(*)
          .returning(mockVirtualMachine).once()
        (mockLoopingTaskRunner.start _).expects().once()
        mockNewScalaVirtualMachineFunc.expects(*, *, *)
          .returning(stub[TestScalaVirtualMachine]).once()
        // MOCK ===============================================================

        val mockProcess = mock[Process]
        (mockVirtualMachine.process _).expects().returning(mockProcess).once()

        launchingDebugger.start((_) => {})

        launchingDebugger.process should be (Some(mockProcess))
      }

      it("should return None if the JVM has not been launched") {
        val launchingDebugger = new TestLaunchingDebugger(shouldJdiLoad = true)

        launchingDebugger.process should be (None)
      }
    }
  }
}
