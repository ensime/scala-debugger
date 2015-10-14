package org.senkbeil.debugger

import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers}
import org.senkbeil.debugger.utils.LogLike
import test.TestUtilities

class LaunchingDebuggerIntegrationSpec  extends FunSpec with Matchers
  with Eventually with TestUtilities with LogLike
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("LaunchingDebugger") {
    it("should be able to start a JVM and connect to it") {
      val launchedJvmConnected = new AtomicBoolean(false)

      val className = "org.senkbeil.test.misc.LaunchingMain"
      val classpath = jvmClasspath
      val jvmOptions = Seq("-classpath", classpath)
      val launchingDebugger = LaunchingDebugger(
        className = className,
        jvmOptions = jvmOptions,
        suspend = false
      )
      launchingDebugger.start { _ => launchedJvmConnected.set(true) }

      // Keep checking back until the launched JVM has been connected
      eventually {
        launchedJvmConnected.get() should be (true)
      }
    }
  }
}
