package org.scaladebugger.api.debuggers

import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{ParallelTestExecution, BeforeAndAfter, FunSpec, Matchers}
import test.{TestUtilities, VirtualMachineFixtures}

class AttachingDebuggerIntegrationSpec extends FunSpec with Matchers
  with BeforeAndAfter with VirtualMachineFixtures
  with TestUtilities
  with ParallelTestExecution
{
  @volatile private var jvmProcess: (Int, Process) = _

  before {
    jvmProcess = createProcess()
  }

  after {
    destroyProcess(jvmProcess._2)
  }

  describe("AttachingDebugger") {
    it("should be able to attach to a running JVM process") {
      val attachingDebugger = AttachingDebugger(jvmProcess._1)

      val attachedToVirtualMachine = new AtomicBoolean(false)

      // Need to keep retrying until process is ready to be attached to
      eventually {
        attachingDebugger.start(_ => attachedToVirtualMachine.set(true))
      }

      // Keep checking back until we have successfully attached
      eventually {
        attachedToVirtualMachine.get() should be (true)
      }
    }
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
