package org.senkbeil.debugger.api.jdi

import java.io.File
import java.net.{URL, URLClassLoader}
import org.senkbeil.debugger.api.utils.LogLike
import scala.util.Try

class JDILoader(
    private val _classLoader: ClassLoader = classOf[JDILoader].getClassLoader
) extends LogLike {
  /** The directory containing JDK libraries relative to the root of the JDK */
  private val BaseLibDir = "lib"

  /** The jar containing the JDI classes */
  private val NeededJdiJar = "tools.jar"

  /** The path to the jar containing JDI relative to the root of the JDK */
  private val JdiJarPath = s"$BaseLibDir/$NeededJdiJar"

  /**
   * Checks if it is possible to use the JDI using either the given class
   * loader or by using a jar located in the JDK (if possible).
   *
   * @param classLoader The class loader to use to check for JDI (default is
   *                    this class's class loader)
   *
   * @return True if JDI is able to be loaded, otherwise false
   */
  def isJdiAvailable(classLoader: ClassLoader = _classLoader): Boolean =
    checkJdiAndGetClassLoader(classLoader)._1

  /**
   * Attempts to ensure that the JDI is loaded. First, checks if the JDI is
   * already available. If not, attempts to find a JDK path and load it.
   *
   * @param classLoader The class loader to use to check for JDI (default is
   *                    this class's class loader)
   *
   * @return True if successful, otherwise false
   */
  def tryLoadJdi(
    classLoader: ClassLoader = _classLoader
  ): Boolean = {
    // If the interface is available, quit early
    if (canJdiBeLoaded(classLoader)) return true

    // Report that we are going to have to "hackily" look around for the JDI
    logger.warn("JDI not found on classpath! Searching standard locations!")

    val validClassLoader = findValidJdiUrlClassLoader(_classLoader)

    // If there was no class loader that worked, exit
    if (validClassLoader.isEmpty) return false

    // Add the valid class loader to our system
    val validJarUrl = validClassLoader.get.getURLs.head

    logger.info(s"Using ${validJarUrl.getFile} for JDI")
    if (!addUrlToSystemClassLoader(validJarUrl)) {
      logger.warn(
        """
          |Found valid tools.jar, but unable to add to system class loader as
          |it is not an instance of a URL class loader!
        """.stripMargin.replace("\n", " "))
    }

    // Final check to ensure that it was loaded into the system class loader
    canJdiBeLoaded(getSystemClassLoader)
  }

  /**
   * Determines if JDI is possible and, if so, returns the class loader
   * responsible for loading it.
   *
   * @param classLoader The initial class loader to use to check if it is
   *                    possible to load JDI
   *
   * @return A tuple indicating whether or not JDI can be loaded along with the
   *         class loader if it can be loaded
   */
  private def checkJdiAndGetClassLoader(
    classLoader: ClassLoader): (Boolean, Option[ClassLoader]
  ) = {
    // If the interface is available, quit early
    if (canJdiBeLoaded(classLoader)) return (true, Some(classLoader))

    // Report that we are going to have to "hackily" look around for the JDI
    logger.warn("JDI not found on classpath! Searching standard locations!")

    val validClassLoader = findValidJdiUrlClassLoader(_classLoader)

    // If there is a class loader that works, we are good to go
    (validClassLoader.nonEmpty, validClassLoader)
  }

  /**
   * Checks if the needed JDK exists on our classpath to use the Java debugger
   * interface.
   *
   * @param classLoader The class loader to use to check for JDI (default is
   *                    this class's class loader)
   *
   * @return True if JDI is able to be loaded, otherwise false
   */
  private def canJdiBeLoaded(
    classLoader: ClassLoader = _classLoader
  ): Boolean = {
    try {
      val rootJdiClass = "com.sun.jdi.Bootstrap"

      // Should throw an exception if JDI is not available
      classForName(rootJdiClass, initialize = false, classLoader = classLoader)

      true
    } catch {
      case _: ClassNotFoundException => false
      case ex: Throwable => throw ex
    }
  }

  /**
   * Attempts to find a class loader that can successfully load the JDI.
   *
   * @param classLoader The class loader to use as the parent for all created
   *                    class loaders (while attempting to find a working one)
   *
   * @return Some class loader if one works, otherwise None
   */
  private def findValidJdiUrlClassLoader(
    classLoader: ClassLoader
  ): Option[URLClassLoader] = {
    // Get path to the Java installation being used to run this debugger
    val potentialJarPaths = findPotentialJdkJarPaths(JdiJarPath)

    logger.trace(s"Found the following potential paths for $NeededJdiJar: " +
      potentialJarPaths.mkString(","))

    // Lookup each path to see if the jar exists
    val validJarFiles = potentialJarPaths.map(new File(_)).filter(_.exists())

    // Attempt loading each jar and checking if JDI is available
    validJarFiles.map(jar => {
      logger.trace(s"Checking $jar for JDI")
      new URLClassLoader(Array(jar.toURI.toURL), classLoader)
    }).find(canJdiBeLoaded)
  }

  /**
   * Attempts to find potential paths for a jar in the JDK.
   *
   * @param jarPath The path to the jar relative to the JDK
   *
   * @return The sequence of potential paths
   */
  protected def findPotentialJdkJarPaths(jarPath: String): Seq[String] = {
    // Try to retrieve paths using various means
    val possiblePaths = Seq(
      Try(System.getenv("JDK_HOME")),
      Try(System.getenv("JAVA_HOME")),
      Try(new File(System.getProperty("java.home")).getParent),
      Try(System.getProperty("java.home"))
    )

    // Find potential valid paths
    val paths = possiblePaths
      .flatMap(_.toOption.flatMap(Option(_))) // Convert null to None
      .map(_.trim)
      .filter(_.nonEmpty)
      .map(_ + "/" + jarPath)
      .distinct

    paths
  }

  /**
   * Adds a url to the system class loader if it represents a URL class loader.
   *
   * @param url The url to add
   *
   * @return True if able to add the url to the system class loader, otherwise
   *         false
   */
  private def addUrlToSystemClassLoader(url: URL) = {
    getSystemClassLoader match {
      case urlClassLoader: URLClassLoader =>
        val addUrlMethod = {
          val method =
            classOf[URLClassLoader].getDeclaredMethod("addURL", classOf[URL])
          method.setAccessible(true)

          method
        }

        // Add the jar to our system class loader
        addUrlMethod.invoke(urlClassLoader, url)

        true

      // Not a URL Class Loader, so cannot add the url
      case _ => false
    }
  }

  /**
   * Attempts to load the specified class.
   *
   * @param name The full name of the class to load
   * @param initialize If true, initializes the class
   * @param classLoader The class loader to use for loading the class, defaults
   *                    to the system class loader
   *
   * @note Exposed for testing!
   *
   * @throws ClassNotFoundException If the class was not found using the
   *                                class loader or any of its parents
   *
   * @return The class loaded by the class loader
   */
  @throws(classOf[ClassNotFoundException])
  protected def classForName(
    name: String,
    initialize: Boolean = true,
    classLoader: ClassLoader = getSystemClassLoader
  ): Class[_] = Class.forName(name, initialize, classLoader)

  /**
   * Retrieves the class loader used by the JVM.
   *
   * @note Wraps ClassLoader.getSystemClassLoader(), used for testing.
   *
   * @return The class loader used by default for the JVM system
   */
  protected def getSystemClassLoader: ClassLoader =
    ClassLoader.getSystemClassLoader
}
