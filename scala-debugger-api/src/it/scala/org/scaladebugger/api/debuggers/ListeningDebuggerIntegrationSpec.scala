package org.scaladebugger.api.debuggers

import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{ParallelTestExecution, BeforeAndAfter, FunSpec, Matchers}
import test.{TestUtilities, VirtualMachineFixtures}

class ListeningDebuggerIntegrationSpec  extends FunSpec with Matchers
  with BeforeAndAfter with VirtualMachineFixtures
  with TestUtilities with Eventually
  with ParallelTestExecution
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  @volatile private var jvmProcesses: Seq[Process] = Nil

  @volatile private var address = ""
  @volatile private var port = 0

  // Before each test, find an available port
  before {
    val socket = new ServerSocket(0)
    address = socket.getInetAddress.getHostName
    port = socket.getLocalPort
    socket.close()
  }

  // After each test, destroy any leftover JVM processes
  after {
    jvmProcesses.foreach(destroyProcess)
  }

  describe("ListeningDebugger") {
    it("should be able to listen for multiple connecting JVM processes") {
      val totalJvmProcesses = 3
      val currentConnectedCount = new AtomicInteger(0)

      // Start listening for JVM connections
      val listeningDebugger = ListeningDebugger(hostname = address, port = port)
      listeningDebugger.start(_ => currentConnectedCount.incrementAndGet())

      // Verify that our listening debugger can actually support multiple
      // connections (it should as a socket listener)
      if (!listeningDebugger.supportsMultipleConnections) {
        alert(
          "Listening debuggers do not support multiple connections on this JVM!"
        )
      }

      // Spawn our JVM processes
      (1 to totalJvmProcesses).foreach(_ => createProcess())

      // Keep checking back until we have successfully connected all JVMs
      eventually {
        currentConnectedCount.get() should be (totalJvmProcesses)
      }
    }
  }

  private def createProcess(): Unit = {
    jvmProcesses +:= JDITools.spawn(
      className = "org.scaladebugger.test.misc.ListeningMain",
      server = false,
      suspend = true,
      port = port
    )
  }

  private def destroyProcess(process: Process): Unit = process.destroy()
}
