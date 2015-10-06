package org.senkbeil.debugger

import java.io.File
import java.net.URLClassLoader
import java.util.concurrent.atomic.AtomicBoolean

import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Milliseconds, Seconds, Span}
import org.scalatest.{FunSpec, Matchers}

class LaunchingDebuggerIntegrationSpec  extends FunSpec with Matchers
  with Eventually
{
  implicit override val patienceConfig = PatienceConfig(
    timeout = scaled(Span(5, Seconds)),
    interval = scaled(Span(5, Milliseconds))
  )

  describe("LaunchingDebugger") {
    it("should be able to listen for multiple connecting JVM processes") {
      val launchedJvmConnected = new AtomicBoolean(false)

      val className = "org.senkbeil.test.misc.LaunchingMain"
      val classpath = ClassLoader.getSystemClassLoader match {
        case u: URLClassLoader =>
          u.getURLs.map(_.getPath).map(new File(_))
            .mkString(System.getProperty("path.separator"))
        case _ => System.getProperty("java.class.path")
      }
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
