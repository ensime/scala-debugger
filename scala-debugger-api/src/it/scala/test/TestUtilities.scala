package test

import java.io.File
import java.net.URLClassLoader

import org.senkbeil.debugger.api.utils.LogLike

/**
 * Contains helper methods for testing.
 */
trait TestUtilities { this: LogLike =>
  /**
   * Converts a class string to a file string.
   *
   * @example org.senkbeil.MyClass becomes org/senkbeil/MyClass.scala
   *
   * @param classString The class string to convert
   *
   * @return The resulting file string
   */
  def scalaClassStringToFileString(classString: String) =
    classString.replace('.', java.io.File.separatorChar) + ".scala"

  /**
   * Executes the block of code and logs the time taken to evaluate it.
   *
   * @param block The block of code to execute
   * @tparam T The return type of the block of code
   *
   * @return The value returned from the block of code
   */
  def logTimeTaken[T](block: => T): T = {
    val startTime = System.currentTimeMillis()

    try {
      block
    } finally {
      val finalTime = System.currentTimeMillis() - startTime
      logger.info(s"Time taken: ${finalTime / 1000.0}s")
    }
  }

  /**
   * Retrieves a JVM classpath string that contains the current classpath.
   * @return
   */
  def jvmClasspath: String = ClassLoader.getSystemClassLoader match {
    case u: URLClassLoader =>
      u.getURLs.map(_.getPath).map(new File(_))
        .mkString(System.getProperty("path.separator"))
    case _ => System.getProperty("java.class.path")
  }

  /**
   * Spawns a new Scala process using the provided class name as the entrypoint.
   *
   * @note Assumes that scala is available on the path!
   *
   * @param className The name of the class to use as the entrypoint for the
   *                  Scala process
   * @param port The port to use for the Scala process to listen on
   * @param hostname Optional hostname to use for the Scala process to listen on
   * @param server Whether or not to launch the process as a server waiting for
   *               a debugger connection or a client connecting to a listening
   *               debugger
   * @param suspend Whether or not to start the process suspended until a
   *                debugger attaches to it or it attaches to a debugger
   * @param args The collection of arguments to pass to the Scala process
   *
   * @return The created Scala process
   */
  def spawn(
    className: String,
    port: Int,
    hostname: String = "",
    server: Boolean = true,
    suspend: Boolean = false,
    args: Seq[String] = Nil
  ): Process = {

    val jdwpString = generateJdwpString(
      port = port,
      address = if (hostname.nonEmpty) Some(hostname) else None,
      suspend = suspend,
      server = server
    )

    val processCollection = Seq(
      "java",
      jdwpString,
      "-classpath", jvmClasspath,
      className
    )

    val processBuilder = new ProcessBuilder
    processBuilder.command(processCollection: _*)
    processBuilder.directory(new File(System.getProperty("user.dir")))

    logger.debug("Launching " + processCollection.mkString(" "))
    processBuilder.start()
  }

  private def generateJdwpString(
    port: Int,
    transport: String = "dt_socket",
    server: Boolean = true,
    suspend: Boolean = false,
    address: Option[String] = None
  ): String = {
    val serverString = if (server) "y" else "n"
    val suspendString = if (suspend) "y" else "n"
    val addressString = address.map(_ + ":").getOrElse("") + port.toString

    "-agentlib:jdwp=" + Seq(
      Seq("transport", transport).mkString("="),
      Seq("server", serverString).mkString("="),
      Seq("suspend", suspendString).mkString("="),
      Seq("address", addressString).mkString("=")
    ).mkString(",")
  }
}
