package org.scaladebugger.api.debuggers
import acyclic.file

import java.net.ServerSocket
import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.utils.JDITools
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{ParallelTestExecution, BeforeAndAfter, FunSpec, Matchers}
import test.{TestUtilities, VirtualMachineFixtures}

class AttachingDebuggerIntegrationSpec extends FunSpec with Matchers
  with BeforeAndAfter with VirtualMachineFixtures
  with TestUtilities with Eventually
  with ParallelTestExecution
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  @volatile private var attachingDebugger : Option[AttachingDebugger] = None

  @volatile private var jvmProcess: (Int, Process) = _

  before {
    jvmProcess = createProcess()
    attachingDebugger = Some(AttachingDebugger(jvmProcess._1))
  }

  after {
    destroyProcess(jvmProcess._2)
    attachingDebugger.foreach(_.stop())
    attachingDebugger = None
  }

  describe("AttachingDebugger") {
    it("should be able to attach to a running JVM process") {
      val attachedToVirtualMachine = new AtomicBoolean(false)

      // Need to keep retrying until process is ready to be attached to
      eventually {
        attachingDebugger.foreach(
          _.start(_ => attachedToVirtualMachine.set(true))
        )
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
