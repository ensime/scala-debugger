package org.scaladebugger.api.debuggers
import acyclic.file

import java.util

import com.sun.jdi.connect.Connector.Argument
import com.sun.jdi.connect.{TransportTimeoutException, Connector, ListeningConnector}
import com.sun.jdi.{VirtualMachine, VirtualMachineManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}
import org.scaladebugger.api.profiles.ProfileManager
import org.scaladebugger.api.utils.LoopingTaskRunner
import org.scaladebugger.api.virtualmachines.StandardScalaVirtualMachine

import scala.collection.JavaConverters._

class ListeningDebuggerSpec extends test.ParallelMockFunSpec
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

  private val testHostname = "localhost"
  private val testPort = 1234
  private val testWorkers = 4

  private implicit val mockVirtualMachineManager = mock[VirtualMachineManager]
  private val mockProfileManager = mock[ProfileManager]
  private val mockLoopingTaskRunner = mock[LoopingTaskRunner]
  private val mockNewScalaVirtualMachineFunc = mockFunction[
    VirtualMachine, ProfileManager, LoopingTaskRunner, StandardScalaVirtualMachine
  ]

  private class TestListeningDebugger(
    override val isAvailable: Boolean = true,
    private val shouldJdiLoad: Boolean = true
  ) extends ListeningDebugger(
    virtualMachineManager = mockVirtualMachineManager,
    newProfileManagerFunc = () => mockProfileManager,
    loopingTaskRunner     = mockLoopingTaskRunner,
    hostname              = testHostname,
    port                  = testPort,
    workers               = testWorkers
  ) {
    /** Exposing as public method. */
    override def listenTask[T](
      connector: ListeningConnector,
      arguments: util.Map[String, Argument],
      defaultProfile: String,
      startProcessingEvents: Boolean,
      newVirtualMachineFunc: (StandardScalaVirtualMachine) => T
    ): Unit = super.listenTask(
      connector,
      arguments,
      defaultProfile,
      startProcessingEvents,
      newVirtualMachineFunc
    )

    override def assertJdiLoaded(): Unit =
      if (!shouldJdiLoad) throw new AssertionError

    override protected def newScalaVirtualMachine(
      virtualMachine: VirtualMachine,
      profileManager: ProfileManager,
      loopingTaskRunner: LoopingTaskRunner
    ): StandardScalaVirtualMachine = mockNewScalaVirtualMachineFunc(
      virtualMachine,
      profileManager,
      loopingTaskRunner
    )
  }
  private val listeningDebugger = new TestListeningDebugger()


  describe("ListeningDebugger") {
    describe("#start") {
      it("should throw an exception if unable to load JDI") {
        val listeningDebugger = new TestListeningDebugger(shouldJdiLoad = false)

        intercept[AssertionError] {
          listeningDebugger.start((_) => {})
        }
      }

      it("should throw an exception if already started") {
        // MOCK ===============================================================
        val mockListeningConnector = mock[ListeningConnector]

        (mockListeningConnector.name _).expects()
          .returning("com.sun.jdi.SocketListen")

        (mockVirtualMachineManager.listeningConnectors _).expects()
          .returning(Seq(mockListeningConnector).asJava)

        (mockListeningConnector.defaultArguments _).expects().returning(Map(
          "localAddress" -> createConnectorArgumentMock(setter = true),
          "port" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockListeningConnector.supportsMultipleConnections _).expects().once()
        (mockListeningConnector.startListening _).expects(*).once()
        (mockLoopingTaskRunner.start _).expects().once()
        (mockLoopingTaskRunner.addTask _).expects(*)
          .repeated(testWorkers).times()
        // MOCK ===============================================================

        listeningDebugger.start((_) => {})

        intercept[AssertionError] {
          listeningDebugger.start((_) => {})
        }
      }

      it("should throw an exception if unable to get the listening connector") {
        val listeningDebugger = new TestListeningDebugger(shouldJdiLoad = true)
        (mockVirtualMachineManager.listeningConnectors _).expects()
          .returning((Nil: Seq[ListeningConnector]).asJava)

        intercept[AssertionError] {
          listeningDebugger.start((_) => {})
        }
      }

      it("should begin listening for virtual machine connections") {
        // MOCK ===============================================================
        val mockListeningConnector = mock[ListeningConnector]

        (mockListeningConnector.name _).expects()
          .returning("com.sun.jdi.SocketListen")

        (mockVirtualMachineManager.listeningConnectors _).expects()
          .returning(Seq(mockListeningConnector).asJava)

        (mockListeningConnector.defaultArguments _).expects().returning(Map(
          "localAddress" -> createConnectorArgumentMock(setter = true),
          "port" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockListeningConnector.supportsMultipleConnections _).expects().once()
        (mockLoopingTaskRunner.start _).expects().once()
        (mockLoopingTaskRunner.addTask _).expects(*)
          .repeated(testWorkers).times()
        // MOCK ===============================================================

        (mockListeningConnector.startListening _).expects(*).once()

        listeningDebugger.start((_) => {})
      }

      it("should spawn X workers to listen for incoming JVM connections") {
        // MOCK ===============================================================
        val mockListeningConnector = mock[ListeningConnector]

        (mockListeningConnector.name _).expects()
          .returning("com.sun.jdi.SocketListen")

        (mockVirtualMachineManager.listeningConnectors _).expects()
          .returning(Seq(mockListeningConnector).asJava)

        (mockListeningConnector.defaultArguments _).expects().returning(Map(
          "localAddress" -> createConnectorArgumentMock(setter = true),
          "port" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockListeningConnector.supportsMultipleConnections _).expects().once()
        (mockListeningConnector.startListening _).expects(*).once()
        (mockLoopingTaskRunner.start _).expects().once()
        // MOCK ===============================================================

        (mockLoopingTaskRunner.addTask _).expects(*)
          .repeated(testWorkers).times()

        listeningDebugger.start((_) => {})
      }
    }

    describe("#isRunning") {
      it("should return true if already started") {
        // MOCK ===============================================================
        val mockListeningConnector = mock[ListeningConnector]

        (mockListeningConnector.name _).expects()
          .returning("com.sun.jdi.SocketListen")

        (mockVirtualMachineManager.listeningConnectors _).expects()
          .returning(Seq(mockListeningConnector).asJava)

        (mockListeningConnector.defaultArguments _).expects().returning(Map(
          "localAddress" -> createConnectorArgumentMock(setter = true),
          "port" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockListeningConnector.supportsMultipleConnections _).expects().once()
        (mockLoopingTaskRunner.start _).expects().once()
        (mockLoopingTaskRunner.addTask _).expects(*)
          .repeated(testWorkers).times()
        (mockListeningConnector.startListening _).expects(*).once()
        // MOCK ===============================================================

        listeningDebugger.start((_) => {})

        listeningDebugger.isRunning should be (true)
      }

      it("should return false if not started (or started and then stopped)") {
        listeningDebugger.isRunning should be (false)
      }
    }

    describe("#stop") {
      it("should throw an exception if not started") {
        intercept[AssertionError] {
          listeningDebugger.stop()
        }
      }

      it("should stop listening for JVM connections and shutdown workers") {
        // MOCK ===============================================================
        val mockListeningConnector = mock[ListeningConnector]

        (mockListeningConnector.name _).expects()
          .returning("com.sun.jdi.SocketListen")

        (mockVirtualMachineManager.listeningConnectors _).expects()
          .returning(Seq(mockListeningConnector).asJava)

        (mockListeningConnector.defaultArguments _).expects().returning(Map(
          "localAddress" -> createConnectorArgumentMock(setter = true),
          "port" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockListeningConnector.supportsMultipleConnections _).expects().once()
        (mockListeningConnector.startListening _).expects(*).once()
        (mockLoopingTaskRunner.start _).expects().once()
        (mockLoopingTaskRunner.addTask _).expects(*)
          .repeated(testWorkers).times()
        // MOCK ===============================================================

        listeningDebugger.start((_) => {})

        val mockVirtualMachine = mock[VirtualMachine]
        (mockVirtualMachineManager.connectedVirtualMachines _).expects()
          .returning(List[VirtualMachine](mockVirtualMachine).asJava).once()
        (mockVirtualMachine.dispose _).expects().once()
        (mockListeningConnector.stopListening _).expects(*).once()
        (mockLoopingTaskRunner.stop _).expects(true).once()

        listeningDebugger.stop()
      }
    }

    describe("#listeningTask") {
      it("should listen for a new connection") {
        val mockListeningConnector = mock[ListeningConnector]
        val mockArguments = mock[java.util.Map[String, Connector.Argument]]
        val mockCallback = mockFunction[StandardScalaVirtualMachine, Unit]

        // Expect accept call (and throw exception to do nothing)
        (mockListeningConnector.accept _).expects(mockArguments)
          .throwing(new TransportTimeoutException).once()

        listeningDebugger.listenTask(
          mockListeningConnector,
          mockArguments,
          "",
          true,
          mockCallback
        )
      }

      it("should create a new ScalaVirtualMachine and pass it to the callback") {
        val mockListeningConnector = mock[ListeningConnector]
        val mockArguments = mock[java.util.Map[String, Connector.Argument]]
        val mockCallback = mockFunction[StandardScalaVirtualMachine, Unit]

        val mockVirtualMachine = mock[VirtualMachine]

        // Expect accept call, returning a new virtual machine instance
        (mockListeningConnector.accept _).expects(mockArguments)
          .returning(mockVirtualMachine).once()

        class TestScalaVirtualMachine extends StandardScalaVirtualMachine(
          mockVirtualMachine, null, null
        )
        val stubScalaVirtualMachine = stub[TestScalaVirtualMachine]
        mockNewScalaVirtualMachineFunc.expects(mockVirtualMachine, *, *)
          .returning(stubScalaVirtualMachine).once()

        mockCallback.expects(stubScalaVirtualMachine).once()

        listeningDebugger.listenTask(
          mockListeningConnector,
          mockArguments,
          "",
          true,
          mockCallback
        )
      }

      it("should apply any pending requests to the created ScalaVirtualMachine") {
        val mockListeningConnector = mock[ListeningConnector]
        val mockArguments = mock[java.util.Map[String, Connector.Argument]]
        val mockVirtualMachine = mock[VirtualMachine]

        val expected = stub[TestScalaVirtualMachine]
        listeningDebugger.addPendingScalaVirtualMachine(expected)

        // Expect accept call, returning a new virtual machine instance
        (mockListeningConnector.accept _).expects(mockArguments)
          .returning(mockVirtualMachine).once()

        class TestScalaVirtualMachine extends StandardScalaVirtualMachine(
          mockVirtualMachine, null, null
        )
        val mockScalaVirtualMachine = mock[TestScalaVirtualMachine]
        mockNewScalaVirtualMachineFunc.expects(mockVirtualMachine, *, *)
          .returning(mockScalaVirtualMachine).once()

        (mockScalaVirtualMachine.processPendingRequests _)
          .expects(expected).once()

        (mockScalaVirtualMachine.initialize _)
          .expects(Debugger.DefaultProfileName, true).once()

        listeningDebugger.listenTask(
          mockListeningConnector,
          mockArguments,
          Debugger.DefaultProfileName,
          true,
          _ => {}
        )
      }


      it("should initialize the created ScalaVirtualMachine") {
        val mockListeningConnector = mock[ListeningConnector]
        val mockArguments = mock[java.util.Map[String, Connector.Argument]]
        val mockVirtualMachine = mock[VirtualMachine]

        // Expect accept call, returning a new virtual machine instance
        (mockListeningConnector.accept _).expects(mockArguments)
          .returning(mockVirtualMachine).once()

        class TestScalaVirtualMachine extends StandardScalaVirtualMachine(
          mockVirtualMachine, null, null
        )
        val mockScalaVirtualMachine = mock[TestScalaVirtualMachine]
        mockNewScalaVirtualMachineFunc.expects(mockVirtualMachine, *, *)
          .returning(mockScalaVirtualMachine).once()

        (mockScalaVirtualMachine.initialize _)
          .expects(Debugger.DefaultProfileName, true).once()

        listeningDebugger.listenTask(
          mockListeningConnector,
          mockArguments,
          Debugger.DefaultProfileName,
          true,
          _ => {}
        )
      }
    }

    describe("#connectedScalaVirtualMachines") {
      it("should return an empty list if the debugger has not connected") {
        listeningDebugger.connectedScalaVirtualMachines should be (empty)
      }

      it("should return a list with all connected virtual machines") {
        val mockListeningConnector = mock[ListeningConnector]
        val mockArguments = mock[java.util.Map[String, Connector.Argument]]
        val mockCallback = mockFunction[StandardScalaVirtualMachine, Unit]
        val mockVirtualMachine = mock[VirtualMachine]

        // Expect accept call, returning a new virtual machine instance
        (mockListeningConnector.accept _).expects(mockArguments)
          .returning(mockVirtualMachine).once()

        class TestScalaVirtualMachine extends StandardScalaVirtualMachine(
          mockVirtualMachine, null, null
        )
        val stubScalaVirtualMachine = stub[TestScalaVirtualMachine]
        mockNewScalaVirtualMachineFunc.expects(mockVirtualMachine, *, *)
          .returning(stubScalaVirtualMachine).once()

        mockCallback.expects(stubScalaVirtualMachine).once()

        listeningDebugger.listenTask(
          mockListeningConnector,
          mockArguments,
          "",
          true,
          mockCallback
        )

        listeningDebugger.connectedScalaVirtualMachines should
          contain (stubScalaVirtualMachine)
      }

      it("should return an empty list if stopped after a virtual machine has connected") {
        // MOCK ===============================================================
        val mockListeningConnector = mock[ListeningConnector]

        (mockListeningConnector.name _).expects()
          .returning("com.sun.jdi.SocketListen")

        (mockVirtualMachineManager.listeningConnectors _).expects()
          .returning(Seq(mockListeningConnector).asJava)

        (mockListeningConnector.defaultArguments _).expects().returning(Map(
          "localAddress" -> createConnectorArgumentMock(setter = true),
          "port" -> createConnectorArgumentMock(setter = true)
        ).asJava)

        (mockListeningConnector.supportsMultipleConnections _).expects().once()
        (mockListeningConnector.startListening _).expects(*).once()
        (mockLoopingTaskRunner.start _).expects().once()
        (mockLoopingTaskRunner.addTask _).expects(*)
          .repeated(testWorkers).times()
        // MOCK ===============================================================

        listeningDebugger.start((_) => {})

        val mockVirtualMachine = mock[VirtualMachine]
        val mockArguments = mock[java.util.Map[String, Connector.Argument]]
        class TestScalaVirtualMachine extends StandardScalaVirtualMachine(
          mockVirtualMachine, null, null
        )

        listeningDebugger.listenTask(
          mockListeningConnector,
          mockArguments,
          "",
          true,
          _ => {}
        )

        (mockVirtualMachineManager.connectedVirtualMachines _).expects()
          .returning(List[VirtualMachine](mockVirtualMachine).asJava).once()
        (mockVirtualMachine.dispose _).expects().once()
        (mockListeningConnector.stopListening _).expects(*).once()
        (mockLoopingTaskRunner.stop _).expects(true).once()

        listeningDebugger.stop()

        listeningDebugger.connectedScalaVirtualMachines should be (empty)
      }
    }

    describe("#connectedVirtualMachines") {
      it("should return a collection of connected virtual machines") {
        val expected = Seq(mock[VirtualMachine], mock[VirtualMachine])

        (mockVirtualMachineManager.connectedVirtualMachines _).expects()
          .returning(expected.asJava)

        listeningDebugger.connectedVirtualMachines should
          contain theSameElementsAs expected
      }
    }
  }
}
