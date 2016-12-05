package org.scaladebugger.api.utils

import org.scalamock.scalatest.MockFactory
import org.scalatest.{FunSpec, Matchers, ParallelTestExecution}

class JDILoaderSpec extends FunSpec with Matchers with ParallelTestExecution
  with MockFactory
{
  private val jdiLoader = new JDILoader()

  describe("JDILoader") {
    describe("#isJdiAvailable") {
      it("should return false if JDI is not available") {
        val expected = false

        // Create a custom JDI loader that never returns any jars for JDI
        val jdiLoader = new JDILoader() {
          override protected def findPotentialJdkJarPaths(
            jarPath: String
          ): Seq[String] = Nil
        }

        // Create a classloader that does not have JDI available
        val classLoader = new ClassLoader(null) {}

        // Attempt to use the classloader to load JDI
        val actual = jdiLoader.isJdiAvailable(classLoader)

        actual should be (expected)
      }

      it("should return true if JDI is available on classpath") {
        val expected = true

        // Use class loader given to JDI loader (defaults to this classloader)
        // NOTE: Assuming that the JDK with tools.jar is on our path when
        //       running this test
        val actual = jdiLoader.isJdiAvailable()

        actual should be (expected)
      }

      it("should return true if JDI is not on classpath but was found elsewhere") {
        val expected = true

        // Create a classloader that does not have JDI available
        val classLoader = new ClassLoader(null) {}

        // Attempt to use the classloader to load JDI, should fall back and
        // find tools.jar (thereby returning true)
        // NOTE: Assuming that JDK with tools.jar is available to be found
        val actual = jdiLoader.isJdiAvailable(classLoader)

        actual should be (expected)
      }
    }

    describe("#tryLoadJdi") {
      it("should return true if JDI can already be loaded") {
        val expected = true

        // Use class loader given to JDI loader (defaults to this classloader)
        // NOTE: Assuming that the JDK with tools.jar is on our path when
        //       running this test
        val actual = jdiLoader.tryLoadJdi()

        actual should be (expected)
      }

      it("should return false if no valid classloader can be found to load it") {
        val expected = false

        // Create a custom JDI loader that never returns any jars for JDI
        val jdiLoader = new JDILoader() {
          override protected def findPotentialJdkJarPaths(
            jarPath: String
          ): Seq[String] = Nil
        }

        // Create a classloader that does not have JDI available
        val classLoader = new ClassLoader(null) {}

        // Attempt to use the classloader to load JDI
        val actual = jdiLoader.tryLoadJdi(classLoader)

        actual should be (expected)
      }

      it("should return false if could not add a valid classloader to system") {
        val expected = false

        // Create a custom JDI loader that has a non-URL class loader retrieved
        // as the system class loader
        val jdiLoader = new JDILoader() {
          override protected def getSystemClassLoader: ClassLoader =
            new ClassLoader(null) {}
        }

        // Create a classloader that does not have JDI available
        val classLoader = new ClassLoader(null) {}

        // Attempt to use the classloader to load JDI
        val actual = jdiLoader.tryLoadJdi(classLoader)

        actual should be (expected)
      }

      it("should return true if could add valid classloader to system") {
        val expected = true

        // Create a classloader that does not have JDI available
        val classLoader = new ClassLoader(null) {}

        // Attempt to use the classloader to load JDI
        // NOTE: Assuming that JDK has tools.jar available to be detected
        val actual = jdiLoader.tryLoadJdi(classLoader)

        actual should be (expected)
      }
    }
  }
}
