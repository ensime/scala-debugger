package org.scaladebugger.api.debuggers
import java.io.IOException

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FunSpec, Matchers, ParallelTestExecution}
import test.{TestUtilities, VirtualMachineFixtures}

class ProcessDebuggerIntegrationSpec extends FunSpec with Matchers
  with BeforeAndAfter with VirtualMachineFixtures
  with TestUtilities
  with ParallelTestExecution
{
  @volatile private var jvmProcess: (Int, Process) = _

  before {
    jvmProcess = createProcess()
  }

  after {
    if (jvmProcess != null) destroyProcess(jvmProcess._2)
  }

  describe("ProcessDebugger") {
    it("should be able to attach to a running JVM process") {
      val processDebugger = ProcessDebugger(jvmProcess._1)

      val attachedToVirtualMachine = new AtomicBoolean(false)

      // Need to keep retrying until process is ready to be attached to
      // NOTE: If unable to connect, ensure that hostname is "localhost"
      eventually {
        processDebugger.start(_ => attachedToVirtualMachine.set(true))
      }

      // Keep checking back until we have successfully attached
      eventually {
        attachedToVirtualMachine.get() should be (true)
      }
    }
  }

  private def createProcess(): (Int, Process) = {
    val (pid, process) = JDITools.spawnAndGetPid(
      className = "org.scaladebugger.test.misc.AttachingMain",
      server = true,
      port = 0 // Assign ephemeral port
    )

    // If unable to retrieve the process PID, exit now
    if (pid <= 0) {
      process.destroy()
      throw new IOException("Unable to retrieve process PID!")
    }

    (pid, process)
  }

  private def destroyProcess(process: Process): Unit = process.destroy()
}
