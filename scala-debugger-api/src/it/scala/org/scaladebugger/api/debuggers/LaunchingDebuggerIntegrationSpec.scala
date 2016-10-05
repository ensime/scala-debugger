package org.scaladebugger.api.debuggers
import acyclic.file

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{ParallelTestExecution, BeforeAndAfter, FunSpec, Matchers}
import org.scaladebugger.api.utils.{JDITools, Logging}
import test.TestUtilities

class LaunchingDebuggerIntegrationSpec  extends FunSpec with Matchers
  with BeforeAndAfter with Eventually with TestUtilities with Logging
  with ParallelTestExecution
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  @volatile private var launchingDebugger : Option[LaunchingDebugger] = None

  // Before each test, initialize a debugger
  before {
    val className = "org.scaladebugger.test.misc.LaunchingMain"
    val classpath = JDITools.jvmClassPath
    val jvmOptions = Seq("-classpath", classpath)
    launchingDebugger = Some(
      LaunchingDebugger(
        className = className,
        jvmOptions = jvmOptions,
        suspend = false
      )
    )
  }

  // After each test, stop and destroy the debugger
  after {
    launchingDebugger.foreach(_.stop())
    launchingDebugger = None
  }

  describe("LaunchingDebugger") {
    it("should be able to start a JVM and connect to it") {
      val launchedJvmConnected = new AtomicBoolean(false)

      launchingDebugger.foreach(
        _.start { _ =>
          launchedJvmConnected.set(true)
        }
      )

      // Keep checking back until the launched JVM has been connected
      eventually {
        launchedJvmConnected.get() should be (true)
      }
    }
  }
}
