package org.scaladebugger.api.utils
import acyclic.file

import java.io.ByteArrayInputStream
import java.net.{URL, URLClassLoader}

import org.scalamock.scalatest.MockFactory
import org.scalatest.{ParallelTestExecution, Matchers, FunSpec}

class JDIToolsSpec extends FunSpec with Matchers with ParallelTestExecution
  with MockFactory
{
  private val jdiTools = new JDITools

  describe("JDITools") {
    describe("#scalaClassStringToFileString") {
      it("should convert the class name to a file name") {
        import java.io.File.{separator => sep}
        val expected = s"some${sep}file${sep}name.scala"

        val actual = jdiTools.scalaClassStringToFileString("some.file.name")

        actual should be (expected)
      }
    }

    describe("#jvmClassPath") {
      it("should return a separated list of urls from the system classloader if it is a url classloader") {
        import java.io.File.{separator => sep}
        val expected = s"somefile.class,somejar.jar,${sep}some${sep}path${sep}to${sep}file.jar"

        val urlClassLoader = new URLClassLoader(
          expected.split(",").map("file:" + _).map(new URL(_))
        )
        val jdiTools = new JDITools {
          override protected def getSystemClassLoader: ClassLoader =
            urlClassLoader
          override protected def getPathSeparator: String = ","
        }

        val actual = jdiTools.jvmClassPath
        actual should be (expected)
      }

      it("should return the value of java.class.path if the system classloader is not a url classloader") {
        val expected = "somefile.class,somejar.jar"
        val jdiTools = new JDITools {
          override protected def getSystemClassLoader: ClassLoader =
            new ClassLoader() {}
          override protected def getJavaClassPath: String = expected
        }

        val actual = jdiTools.jvmClassPath
        actual should be (expected)
      }
    }

    describe("#spawn") {
      it("should set the jdwp string using generateJdwpString") {
        val expected = "some jdwp string"
        val className = "some.class.name"
        val port = 9999
        val server = false
        val suspend = true
        val hostname = "somehostname"

        val stubJdiProcess = stub[JDIProcess]
        val mockGenerateJdwpString = mockFunction[
          Int, String, Boolean, Boolean, String, String
        ]
        val jdiTools = new JDITools {
          override protected def newJDIProcess(): JDIProcess = stubJdiProcess
          override def generateJdwpString(
            port: Int,
            transport: String,
            server: Boolean,
            suspend: Boolean,
            hostname: String
          ): String = mockGenerateJdwpString(port, transport, server, suspend, hostname)
        }

        mockGenerateJdwpString.expects(port, *, server, suspend, hostname)
          .returning(expected).once()
        (stubJdiProcess.setJdwpString _).when(expected).once()

        jdiTools.spawn(className, port, server = server, suspend = suspend, hostname = hostname)
      }

      it("should set the classpath using jvmClassPath") {
        val expected = "some,jvm,classpath"
        val className = "some.class.name"
        val port = 9999

        val stubJdiProcess = stub[JDIProcess]
        val jdiTools = new JDITools {
          override def jvmClassPath: String = expected

          override protected def newJDIProcess(): JDIProcess = stubJdiProcess
        }

        (stubJdiProcess.setClassPath _).when(expected).once()

        jdiTools.spawn(className, port)
      }

      it("should set the class name using the provided class name") {
        val className = "some.class.name"
        val port = 9999

        val stubJdiProcess = stub[JDIProcess]
        val jdiTools = new JDITools {
          override protected def newJDIProcess(): JDIProcess = stubJdiProcess
        }

        (stubJdiProcess.setClassName _).when(className).once()

        jdiTools.spawn(className, port)
      }

      it("should set the process directory to the user directory") {
        import java.io.File.{separator => sep}
        val expected = s"some${sep}user${sep}dir"
        val className = "some.class.name"
        val port = 9999

        val stubJdiProcess = stub[JDIProcess]
        val jdiTools = new JDITools {
          override protected def getUserDir: String = expected

          override protected def newJDIProcess(): JDIProcess = stubJdiProcess
        }

        (stubJdiProcess.setDirectory _).when(expected).once()

        jdiTools.spawn(className, port)
      }

      it("should set the command line arguments using the provided arguments") {
        val expected = Seq("-arg", "3", "--some-arg", "--other-arg=3")

        val stubJdiProcess = stub[JDIProcess]
        val jdiTools = new JDITools {
          override protected def newJDIProcess(): JDIProcess = stubJdiProcess
        }

        (stubJdiProcess.setArguments _).when(expected).once()

        jdiTools.spawn("", 0, args = expected)
      }

      it("should set the JVM options using the provided options") {
        val expected = Seq("-Dsomeproperty=3")

        val stubJdiProcess = stub[JDIProcess]
        val jdiTools = new JDITools {
          override protected def newJDIProcess(): JDIProcess = stubJdiProcess
        }

        (stubJdiProcess.setJvmOptions _).when(expected).once()

        jdiTools.spawn("", 0, options = expected)
      }

      it("should start the process") {
        val className = "some.class.name"
        val port = 9999

        val stubJdiProcess = stub[JDIProcess]
        val jdiTools = new JDITools {
          override protected def newJDIProcess(): JDIProcess = stubJdiProcess
        }

        (stubJdiProcess.start _).when().once()

        jdiTools.spawn(className, port)
      }
    }

    describe("#javaProcesses") {
      it("should parse empty input as an empty list") {
        val expected = Nil

        val input = ""

        val stubProcess = stub[Process]
        val jdiTools = new JDITools {
          override protected def spawnJavaProcessRetrieval(): Process = stubProcess
        }

        val stringStream = new ByteArrayInputStream(input.getBytes("UTF-8"))
        (stubProcess.getInputStream _).when().returns(stringStream).once()

        val actual = jdiTools.javaProcesses()

        actual should be (expected)
      }

      it("should parse each line of input from a list as a Java process") {
        val expected = Seq(
          JavaProcess(0, "a", JVMOptions.Blank),
          JavaProcess(1, "b", JVMOptions.Blank)
        )

        val input = Seq("line1", "line2").mkString("\n")

        val stubProcess = stub[Process]
        val jdiTools = new JDITools {
          override protected def spawnJavaProcessRetrieval(): Process = stubProcess
        }

        val stringStream = new ByteArrayInputStream(input.getBytes("UTF-8"))
        (stubProcess.getInputStream _).when().returns(stringStream).once()

        val actual = jdiTools.javaProcesses((() => {
          var current = expected

          (s: String) => {
            val next = current.headOption
            current = current.takeRight(current.length - 1)
            next
          }
        })())

        actual should be (expected)
      }
    }

    describe("#generateJdwpString") {
      it("should set server=y if server flag is true") {
        val port = 9999
        val jdwpString = jdiTools.generateJdwpString(port, server = true)

        jdwpString should include ("server=y")
      }

      it("should set server=n if server flag is false") {
        val port = 9999
        val jdwpString = jdiTools.generateJdwpString(port, server = false)

        jdwpString should include ("server=n")
      }

      it("should set suspend=y if suspend flag is true") {
        val port = 9999
        val jdwpString = jdiTools.generateJdwpString(port, suspend = true)

        jdwpString should include ("suspend=y")
      }

      it("should set suspend=n if suspend flag is false") {
        val port = 9999
        val jdwpString = jdiTools.generateJdwpString(port, suspend = false)

        jdwpString should include ("suspend=n")
      }

      it("should set address=<hostname>:port if hostname not empty") {
        val hostname = "somehostname"
        val port = 9999
        val jdwpString = jdiTools.generateJdwpString(port, hostname = hostname)

        jdwpString should include (s"address=$hostname:$port")
      }

      it("should set address=port if hostname empty") {
        val port = 9999
        val jdwpString = jdiTools.generateJdwpString(port, hostname = "")

        jdwpString should include (s"address=$port")
      }

      it("should start with -agentlib:jdwp=") {
        val port = 9999
        val jdwpString = jdiTools.generateJdwpString(port)

        jdwpString should startWith ("-agentlib:jdwp=")
      }
    }
  }
}
