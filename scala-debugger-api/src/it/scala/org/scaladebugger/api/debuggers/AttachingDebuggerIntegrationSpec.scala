package org.scaladebugger.api.debuggers

import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.utils.JDITools
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.{ApiTestUtilities, VirtualMachineFixtures}

import scala.util.Try

class AttachingDebuggerIntegrationSpec extends ParallelMockFunSpec
  with VirtualMachineFixtures
  with ApiTestUtilities
{
  describe("AttachingDebugger") {
    it("should be able to attach to a running JVM process") {
      withProcess((port, process) => {
        val attachingDebugger = AttachingDebugger(port)

        val attachedToVirtualMachine = new AtomicBoolean(false)

        // Need to keep retrying until process is ready to be attached to
        eventually {
          attachingDebugger.start(_ => attachedToVirtualMachine.set(true))
        }

        // Keep checking back until we have successfully attached
        eventually {
          attachedToVirtualMachine.get() should be (true)
        }
      })
    }
  }

  /** Port, Process */
  private def withProcess[T](testCode: (Int, Process) => T): T = {
    val jvmProcess = createProcess()

    val result = Try(testCode(jvmProcess._1, jvmProcess._2))

    destroyProcess(jvmProcess._2)

    result.get
  }

  private def createProcess(): (Int, Process) = {
    val port = {
      val socket = new ServerSocket(0)
      val _port = socket.getLocalPort
      socket.close()
      _port
    }

    (port, JDITools.spawn(
      className = "org.scaladebugger.test.misc.AttachingMain",
      server = true,
      suspend = true,
      port = port
    ))
  }

  private def destroyProcess(process: Process): Unit = process.destroy()
}
