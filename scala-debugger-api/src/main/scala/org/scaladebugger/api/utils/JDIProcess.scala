package org.scaladebugger.api.utils

import java.io.File

/**
 * Represents a process started with appropriate configuration for use with JDI.
 */
class JDIProcess extends Logging {
  private val processBuilder = new ProcessBuilder
  private val commandProg = "java"
  private var _className: Option[String] = None
  private var _jvmOptions: Seq[String] = Nil
  private var _classPath: Option[String] = None
  private var _jdwpString: Option[String] = None
  private var _directory: Option[String] = None
  private var _arguments: Seq[String] = Nil

  /**
   * Sets the name of the class to serve as the entrypoint for the JVM process.
   *
   * @param className The full class name used as the entrypoint of the process
   *
   * @return The updated JDI process
   */
  def setClassName(className: String): JDIProcess = {
    _className = Some(className)
    this
  }

  /**
   * Sets the class path of the JVM process.
   *
   * @param classPath The class path used by the process
   *
   * @return The updated JDI process
   */
  def setClassPath(classPath: String): JDIProcess = {
    _classPath = Some(classPath)
    this
  }

  /**
   * Sets the JDWP string used by the JVM process.
   *
   * @param jdwpString The JDWP string used by the process
   *
   * @return The updated JDI process
   */
  def setJdwpString(jdwpString: String): JDIProcess = {
    _jdwpString = Some(jdwpString)
    this
  }

  /**
   * Sets the directory where the JVM process will run.
   *
   * @param directory The directory serving as the root location of the JVM
   *                  process
   *
   * @return The updated JDI process
   */
  def setDirectory(directory: String): JDIProcess = {
    _directory = Some(directory)
    this
  }

  /**
   * Sets any additional JVM options to use with the Scala process.
   *
   * @param options The collection of options to use with the JVM
   *
   * @return The updated JDI process
   */
  def setJvmOptions(options: Seq[String]): JDIProcess = {
    _jvmOptions = options
    this
  }

  /**
   * Sets the arguments passed to the Scala process.
   *
   * @param arguments The collection of arguments to serve as input to the
   *                  Scala process
   *
   * @return The updated JDI process
   */
  def setArguments(arguments: Seq[String]): JDIProcess = {
    _arguments = arguments
    this
  }

  /**
   * Starts the JDI process. Requires all configuration settings to be provided
   * beforehand.
   *
   * @return The Java process resulting from the start of the JDI process
   */
  def start(): Process = {
    require(_className.nonEmpty)
    require(_classPath.nonEmpty)
    require(_jdwpString.nonEmpty)
    require(_directory.nonEmpty)

    var processCollection = Seq(commandProg)

    processCollection ++= _jvmOptions
    _jdwpString.foreach(processCollection :+= _)
    _classPath.foreach(processCollection ++= Seq("-classpath", _))
    _className.foreach(processCollection :+= _)
    processCollection ++= _arguments

    processBuilder.command(processCollection: _*)
    _directory.map(new File(_)).foreach(processBuilder.directory)

    logger.debug("Launching " + processCollection.mkString(" "))
    processBuilder.start()
  }
}
