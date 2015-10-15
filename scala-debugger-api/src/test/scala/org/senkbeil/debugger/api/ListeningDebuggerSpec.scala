package org.senkbeil.debugger.api

import java.util.concurrent.ExecutorService

import com.sun.jdi.connect.{ListeningConnector, Connector}
import com.sun.jdi.{VirtualMachine, VirtualMachineManager}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{OneInstancePerTest, Matchers, FunSpec}

import scala.collection.JavaConverters._

class ListeningDebuggerSpec extends FunSpec with Matchers
  with OneInstancePerTest with MockFactory
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

  private val testAddress = "localhost"
  private val testPort = 1234
  private val testWorkers = 4

  private val mockExecutorService = mock[ExecutorService]

  private implicit val mockVirtualMachineManager = mock[VirtualMachineManager]
  private class TestListeningDebugger(
    override val isAvailable: Boolean = true,
    private val shouldJdiLoad: Boolean = true
  ) extends ListeningDebugger(
    virtualMachineManager = mockVirtualMachineManager,
    address = testAddress,
    port = testPort,
    executorServiceFunc = () => mockExecutorService,
    workers = testWorkers
  ) {
    override def assertJdiLoaded(): Unit =
      if (!shouldJdiLoad) throw new AssertionError
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
        (mockExecutorService.execute _).expects(*).repeated(testWorkers).times()
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
        (mockExecutorService.execute _).expects(*)
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
        // MOCK ===============================================================

        (mockExecutorService.execute _).expects(*)
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
        (mockExecutorService.execute _).expects(*)
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
        (mockExecutorService.execute _).expects(*).repeated(testWorkers).times()
        // MOCK ===============================================================

        listeningDebugger.start((_) => {})

        (mockListeningConnector.stopListening _).expects(*).once()
        (mockExecutorService.shutdownNow _).expects().once()

        listeningDebugger.stop()
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
