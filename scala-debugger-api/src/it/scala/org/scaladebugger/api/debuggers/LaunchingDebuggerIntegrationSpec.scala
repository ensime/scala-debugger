package org.scaladebugger.api.debuggers

import java.util.concurrent.atomic.AtomicBoolean

import org.scaladebugger.api.utils.{JDITools, Logging}
import org.scaladebugger.test.helpers.ParallelMockFunSpec
import test.ApiTestUtilities

class LaunchingDebuggerIntegrationSpec extends ParallelMockFunSpec
   with ApiTestUtilities with Logging
{
  describe("LaunchingDebugger") {
    it("should be able to start a JVM and connect to it") {
      val launchedJvmConnected = new AtomicBoolean(false)

      val className = "org.scaladebugger.test.misc.LaunchingMain"
      val classpath = JDITools.jvmClassPath
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
