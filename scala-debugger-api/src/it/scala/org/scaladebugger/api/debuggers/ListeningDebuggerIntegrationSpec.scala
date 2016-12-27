package org.scaladebugger.api.debuggers

import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicInteger

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

import scala.util.Try

class ListeningDebuggerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("ListeningDebugger") {
    it("should be able to listen for multiple connecting JVM processes") {
      withProcessCreator((address, port, createProcess) => {
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
      })
    }
  }

  /** Address, Port, Create Process Func */
  private def withProcessCreator[T](testCode: (String, Int, () => Process) => T): T = {
    val (address, port) = {
      val socket = new ServerSocket(0)
      val _address = socket.getInetAddress.getHostName
      val _port = socket.getLocalPort
      socket.close()
      (_address, _port)
    }

    var jvmProcesses: Seq[Process] = Nil
    def createProcess(port: Int): Process = {
      val process = JDITools.spawn(
        className = "org.scaladebugger.test.misc.ListeningMain",
        server = false,
        suspend = true,
        port = port
      )
      jvmProcesses +:= process
      process
    }

    val result = Try(testCode(address, port, () => createProcess(port)))

    // Clean up any leftover processes
    jvmProcesses.foreach(p => Try(p.destroy()))

    result.get
  }
}
