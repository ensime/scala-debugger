package org.scaladebugger.api.utils
import java.io.{BufferedReader, File, IOException, InputStreamReader}
import java.net.URLClassLoader

/**
 * Exposes utility methods related to the Java Debugger Interface.
 */
object JDITools extends JDITools

/**
 * Contains utility methods related to the Java Debugger Interface.
 */
class JDITools private[utils] extends JDILoader with Logging {
  /**
   * Converts a class string to a file string.
   *
   * @example org.senkbeil.MyClass becomes org/senkbeil/MyClass.scala
   * @param classString The class string to convert
   * @return The resulting file string
   */
  def scalaClassStringToFileString(classString: String) =
    classString.replace('.', java.io.File.separatorChar) + ".scala"

  /**
   * Retrieves a JVM classpath string that contains the current classpath.
   *
   * @return The classpath as a string
   */
  def jvmClassPath: String = getSystemClassLoader match {
    case u: URLClassLoader =>
      u.getURLs.map(_.getPath).map(new File(_)).mkString(getPathSeparator)
    case _ => getJavaClassPath
  }

  /**
   * Finds an open port and provides it to the specified function. This
   * method is synchronized to prevent other threads within the same
   * application from accidentally taking the same port when using this method;
   * however, this does not prevent external applications from consuming the
   * provided port.
   *
   * @throws IOException When no port is available
   *
   * @param f The function to evaluate, taking the open port as its argument
   * @tparam T The return value from the function to evaluate
   * @return The result of the evaluated function
   */
  @throws[IOException]
  def usingOpenPort[T](f: Int => T): T = synchronized {
    findOpenPort().map(f)
      .getOrElse(throw new IOException("No port available!"))
  }

  /**
   * Returns a random port that is currently open. Note that there is no
   * safety condition preventing this port from being taken later.
   *
   * @return The number of the port
   */
  def findOpenPort(): Option[Int] = {
    import java.io.IOException
    import java.net.ServerSocket

    // Open an available port, get the number, and close it
    try {
      val socket = new ServerSocket(0)
      socket.setReuseAddress(true)
      val port = socket.getLocalPort
      socket.close()
      Some(port)
    } catch {
      case _: IOException => None
    }
  }

  /**
   * Retrieves the system classloader.
   *
   * @return The system classloader instance
   */
  override protected def getSystemClassLoader: ClassLoader =
    ClassLoader.getSystemClassLoader

  /**
   * Retrieves the system property for path.separator.
   *
   * @return The string representing the 'path.separator' system property
   */
  protected def getPathSeparator: String = System.getProperty("path.separator")

  /**
   * Retrieves the system property for java.class.path.
   *
   * @return The string representing the 'java.class.path' system property
   */
  protected def getJavaClassPath: String = System.getProperty("java.class.path")

  /**
   * Spawns a new Scala process using the provided class name as the entrypoint.
   *
   * @note Assumes that Scala is available on the path!
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
   * @param options Any additional JVM options to pass to the Scala process
   * @return The created Scala process
   */
  def spawn(
    className: String,
    port: Int,
    hostname: String = "",
    server: Boolean = true,
    suspend: Boolean = false,
    args: Seq[String] = Nil,
    options: Seq[String] = Nil
  ): Process = {
    val jdwpString = generateJdwpString(
      port = port,
      hostname = hostname,
      suspend = suspend,
      server = server
    )

    val jdiProcess = newJDIProcess()
    jdiProcess.setJdwpString(jdwpString)
    jdiProcess.setClassPath(jvmClassPath)
    jdiProcess.setClassName(className)
    jdiProcess.setDirectory(getUserDir)
    jdiProcess.setArguments(args)
    jdiProcess.setJvmOptions(options)

    jdiProcess.start()
  }

  /**
   * Spawns a new Scala process using the provided class name as the
   * entrypoint. Retrieves the PID of the process. The spawned JVM cannot
   * start suspended.
   *
   * @note Assumes that Scala is available on the path!
   * @param className The name of the class to use as the entrypoint for the
   *                  Scala process
   * @param port The port to use for the Scala process to listen on
   * @param hostname Optional hostname to use for the Scala process to listen on
   * @param server Whether or not to launch the process as a server waiting for
   *               a debugger connection or a client connecting to a listening
   *               debugger
   * @param args The collection of arguments to pass to the Scala process
   * @param options Any additional JVM options to pass to the Scala process
   * @return The tuple containing the PID (or 0 if failed to retrieve) and
   *         the Scala process
   */
  def spawnAndGetPid(
    className: String,
    port: Int,
    hostname: String = "",
    server: Boolean = true,
    args: Seq[String] = Nil,
    options: Seq[String] = Nil
  ): (Int, Process) = {
    val quote = '"'
    val uniqueId = java.util.UUID.randomUUID().toString
    val process = JDITools.spawn(
      className = className,
      port = port,
      hostname = hostname,
      server = server,
      suspend = false, // Must be false to show up in JPS
      args = args,
      options = options :+ s"-Dscala.debugger.id=$quote$uniqueId$quote"
    )

    val pid = JDITools.javaProcesses()
      .find(_.jvmOptions.properties
        .get("scala.debugger.id").exists(_ == uniqueId)
      ).map(_.pid.toInt).getOrElse(0)

    (pid, process)
  }

  /**
   * Collects a list of active Java processes using the JPS tool.
   *
   * @note Will fail if the JPS tool is not on PATH.
   *
   * @param javaProcessFunc Optional function to convert line of text into a
   *                        Java process instance
   * @return The collection of active Java processes
   */
  def javaProcesses(
    javaProcessFunc: String => Option[JavaProcess] =
      JavaProcess.fromJpsString(_: String)
  ): Seq[JavaProcess] = {
    val p = spawnJavaProcessRetrieval()
    val reader = new BufferedReader(new InputStreamReader(p.getInputStream))

    val stream = Stream.continually(reader.readLine()).takeWhile(_ != null)
    val jProcesses = stream.map(_.trim).force.flatMap(javaProcessFunc(_))

    reader.close()
    Seq(jProcesses: _*)
  }

  /**
   * Spawns a new process to retrieve the list of Java processes.
   *
   * @return The process instance
   */
  protected def spawnJavaProcessRetrieval(): Process =
    Runtime.getRuntime.exec("jps -vl")

  /**
   * Creates a new JDI process instance.
   *
   * @return The new JDI process instance
   */
  protected def newJDIProcess(): JDIProcess = new JDIProcess

  /**
   * Retrieves the system property for user.dir.
   *
   * @return The string representing the 'user.dir' system property
   */
  protected def getUserDir: String = System.getProperty("user.dir")

  /**
   * Generates a JDWP string for use when launching JVMs.
   *
   * @param port The port used to connect to a debugger or listen for debugger
   *             connections
   * @param transport The means of communication (defaults to dt_socket)
   * @param server If true, indicates that the target JVM should run as a
   *               server listening on the provided port for debugger
   *               connections; if false, indicates that the target JVM should
   *               connect to a debugger using the provided port
   * @param suspend If true, indicates that the target JVM should start up
   *                suspended until a connection with a debugger has been
   *                established
   * @param hostname If provided, used as the hostname to connect or bind
   *                 to depending on the server flag
   * @return The string representing the JDWP settings
   */
  def generateJdwpString(
    port: Int,
    transport: String = "dt_socket",
    server: Boolean = true,
    suspend: Boolean = false,
    hostname: String = ""
  ): String = {
    val serverString = if (server) "y" else "n"
    val suspendString = if (suspend) "y" else "n"
    val hostnameString = if (hostname.nonEmpty) hostname + ":" else ""
    val addressString = hostnameString + port.toString

    "-agentlib:jdwp=" + Seq(
      Seq("transport", transport).mkString("="),
      Seq("server", serverString).mkString("="),
      Seq("suspend", suspendString).mkString("="),
      Seq("address", addressString).mkString("=")
    ).mkString(",")
  }
}
