package org.scaladebugger.docs

import java.nio.charset.Charset
import java.nio.file._
import java.util.concurrent.TimeUnit

import com.vladsch.flexmark.ext.front.matter.{AbstractYamlFrontMatterVisitor, YamlFrontMatterExtension}
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import org.scaladebugger.docs.layouts.partials.common.MenuItem
import org.scaladebugger.docs.layouts.{Context, Layout}

import scala.annotation.tailrec
import scala.collection.JavaConverters._
import scala.util.Try

/**
 * Represents a generator of content based on a configuration.
 *
 * @param config The configuration to use when generating files
 */
class Generator(private val config: Config) {
  /** Logger for this class. */
  private val logger = new Logger(this.getClass)

  /** Represents a matcher for markdown paths. */
  private lazy val MarkdownMatcher =
    FileSystems.getDefault.getPathMatcher("glob:**.md")

  /** Represents extensions for the parser and renderer. */
  private lazy val extensions = Seq(YamlFrontMatterExtension.create()).asJava

  /** Represents the Markdown parser. */
  private lazy val parser = Parser.builder().extensions(extensions).build()

  /** Represents the Markdown => HTML renderer. */
  private lazy val renderer =
    HtmlRenderer.builder().extensions(extensions).build()

  /**
   * Runs the generator.
   */
  def run(): Unit = {
    val outputDir = config.outputDir()

    val inputDir = config.inputDir()
    val srcDir = config.srcDir()
    val staticDir = config.staticDir()

    val outputDirPath = Paths.get(outputDir)
    outputDirPath.getFileName

    // Re-create the output directory
    deletePath(outputDirPath)
    Files.createDirectories(outputDirPath)

    // Copy all static content
    val staticDirPath = Paths.get(inputDir, staticDir)
    copyDirectoryContents(staticDirPath, outputDirPath)

    // Process all markdown files
    val srcDirPath = Paths.get(inputDir, srcDir)
    val mdFiles = markdownFiles(srcDirPath)

    // Find all directories of src dir
    val directoryPaths = directories(srcDirPath)

    // Generate top-level menu items based on src dir
    def createLinkedMenuItem(path: Path): MenuItem = {
      MenuItem(
        name = path.getFileName.toString,
        link = "/" + srcDirPath.relativize(path).toString
          .replaceAllLiterally(java.io.File.separator, "/") + "/",
        children = directoryPaths.filter(_.getParent == path)
          .map(createLinkedMenuItem)
      )
    }
    val linkedMenuItems = directoryPaths
      .filter(_.getParent == srcDirPath)
      .map(createLinkedMenuItem)

    // Create our layout context
    val context = Context(
      mainMenuItems = linkedMenuItems,
      sideMenuItems = linkedMenuItems
    )

    // For each markdown file, generate its content and produce a file
    mdFiles.foreach(mdFile => processMarkdownFile(
      mdFile = mdFile,
      context = context,
      srcDirPath = srcDirPath,
      outputDirPath = outputDirPath
    ))
  }

  /**
   * Processes a markdown file into HTML.
   *
   * @param mdFile The path to the markdown file to process
   * @param context The context to use when processing
   * @param srcDirPath The root source directory containing the markdown file
   * @param outputDirPath The root output directory to write the HTML
   */
  private def processMarkdownFile(
    mdFile: Path,
    context: Context,
    srcDirPath: Path,
    outputDirPath: Path
  ): Unit = {
    val startTime = System.nanoTime()

    Try({
      logger.log(s"(( Processing $mdFile ))")

      val relativePath = srcDirPath.relativize(mdFile)
      val fileName = stripExtension(mdFile.getFileName.toString)

      // Create the output path to the new html file's directory
      val outputPath = Option(relativePath.getParent)
        .map(outputDirPath.resolve)
        .getOrElse(outputDirPath)
      val htmlDirPath =
        if (isIndexFile(mdFile)) outputPath
        else outputPath.resolve(fileName)
      Files.createDirectories(htmlDirPath)

      // Create the path to the html file itself
      val htmlFilePath = htmlDirPath.resolve("index.html")

      // Parse the md file into a node
      logger.log(s"\tParsing markdown file")
      val markdownDocument = parser.parseReader(
        Files.newBufferedReader(mdFile, Charset.forName("UTF-8"))
      )

      // Load front matter of document
      logger.log(s"\tExtracting front matter from markdown file")
      val yamlVisitor = new AbstractYamlFrontMatterVisitor
      yamlVisitor.visit(markdownDocument)
      val documentFrontMatter = yamlVisitor.getData.asScala

      // Load layout for file
      val layoutName = documentFrontMatter.get("layout")
        .flatMap(_.asScala.headOption)
      val layout = layoutName match {
        case Some(name) =>
          logger.log(s"\tLoading layout $name")
          layoutFromClassName(name, context)
        case None =>
          logger.log(s"\tLoading default layout")
          defaultLayout(context)
      }

      // Render markdown content to html
      logger.log(s"\tRendering markdown as html")
      val markdownContent = renderer.render(markdownDocument)

      // Apply associated layout
      logger.log(s"\tApplying layout ${layout.getClass.getName}")
      val htmlDocumentContent = layout.toString(markdownContent)

      // Write the md file as html to a file
      logger.log(s"\tWriting to ${htmlFilePath.toString}")
      writeText(htmlFilePath, htmlDocumentContent)
    }).failed.foreach(t => {
      val errorName = t.getClass.getName
      val errorMessage = Option(t.getLocalizedMessage).getOrElse("<none>")
      val depth = config.stackTraceDepth()
      val stackTrace =
        if (depth < 0) t.getStackTrace
        else t.getStackTrace.take(depth)

      logger.error(s"\t!!! Failed: " + errorName)
      logger.error(s"\t!!! Message: " + errorMessage)
      stackTrace.foreach(ste => logger.error(s"\t!!! $ste"))
    })

    val endTime = System.nanoTime()

    // Nano is 10^-9
    val timeTaken = endTime - startTime
    val timeTakenMillis = TimeUnit.NANOSECONDS.toMillis(timeTaken)
    val timeTakenNanosRemainder =
      timeTaken - TimeUnit.MILLISECONDS.toNanos(timeTakenMillis)
    logger.log(s"\tTook $timeTakenMillis.${timeTakenNanosRemainder}ms")
  }

  /**
   * Retrieves the default layout.
   *
   * @param context The context to provide to the default layout
   * @return The default layout instance
   */
  private def defaultLayout(context: Context): Layout = {
    val defaultLayoutClassName = config.defaultLayout()
    layoutFromClassName(defaultLayoutClassName, context)
  }

  /**
   * Creates a new instance of the specified layout class.
   *
   * @param className The fully-qualified class name
   * @param context The context to provide to the layout
   * @return The new layout instance
   * @throws ClassNotFoundException If the class is missing
   * @throws RuntimeException If the specified class is not a layout
   */
  @throws[ClassNotFoundException]
  @throws[RuntimeException]
  private def layoutFromClassName(
    className: String,
    context: Context
  ): Layout = {
    // Retrieve the layout class
    val layoutClass = Class.forName(className)

    // Validate the provided class is of the layout type
    if (!Layout.classIsLayout(layoutClass)) {
      val n = classOf[Layout].getName
      val dn = layoutClass.getName
      throw new RuntimeException(s"$dn is not an instance of $n")
    }

    val layout = layoutClass.newInstance().asInstanceOf[Layout]
    layout.context_=(context)
    layout
  }

  /**
   * Writes the text to the specified path.
   *
   * @param path The path to the file to write
   * @param text The text for the file
   */
  private def writeText(path: Path, text: String): Unit = {
    Files.write(
      path,
      text.getBytes("UTF-8"),
      StandardOpenOption.CREATE,
      StandardOpenOption.WRITE
    )
  }

  /**
   * Removes the file extension from the name.
   *
   * @param fileName The file name whose extension to remove
   * @return The file name without the extension
   */
  private def stripExtension(fileName: String): String = {
    fileName.replaceFirst("[.][^.]+$", "")
  }

  /**
   * Determines whether or not the path represents an index file.
   *
   * @param path The path to inspect
   * @return True if an index file, otherwise false
   */
  private def isIndexFile(path: Path): Boolean = {
    Files.isRegularFile(path) &&
    stripExtension(path.getFileName.toString) == "index"
  }

  /**
   * Retrieves all markdown files found in the specified
   * directories or any of their subdirectories.
   *
   * @param paths The paths to the directories to traverse
   * @return An iterable collection of paths to markdown files
   */
  @tailrec private def markdownFiles(paths: Path*): Iterable[Path] = {
    val topLevelMarkdownFiles = paths.filter(MarkdownMatcher.matches)
    val searchPaths = paths.filterNot(MarkdownMatcher.matches)
    val contents = searchPaths.flatMap(directoryContents)

    val nonMarkdownContents = contents.filterNot(MarkdownMatcher.matches)
    val markdownContents = contents.filter(MarkdownMatcher.matches)

    val allMarkdownFiles = topLevelMarkdownFiles ++ markdownContents
    val allFiles = allMarkdownFiles ++ nonMarkdownContents

    if (nonMarkdownContents.isEmpty) allMarkdownFiles
    else markdownFiles(allFiles: _*)
  }

  /**
   * Lists the contents of a directory.
   *
   * @param path The path to the directory
   * @return An iterable collection of paths to content, or empty if
   *         the provided path is not a directory
   */
  private def directoryContents(path: Path): Iterable[Path] = {
    import scala.collection.JavaConverters._

    if (!Files.isDirectory(path)) Nil
    else Files.newDirectoryStream(path).asScala
  }

  /**
   * Retrieves directories from the specified path.
   *
   * @param path The path whose directories to retrieve
   * @param recursive If true, also retrieves subdirectories
   * @return An iterable collection of paths to directories, or empty if
   *         the provided path is not a directory
   */
  private def directories(
    path: Path,
    recursive: Boolean = true
  ): Seq[Path] = {
    import scala.collection.mutable
    val pathQueue = mutable.Queue[Path]()
    val directories = mutable.Buffer[Path]()

    pathQueue.enqueue(path)
    while (pathQueue.nonEmpty) {
      val p = pathQueue.dequeue()

      if (Files.isDirectory(p)) {
        directories.append(p)
        pathQueue.enqueue(directoryContents(p).toSeq: _*)
      }
    }

    // Remove provided directory
    directories.distinct.filterNot(_ == path)
  }

  /**
   * Deletes the content at the specified path.
   *
   * @param path The path to the directory or file to delete
   */
  private def deletePath(path: Path): Unit = {
    if (Files.exists(path)) {
      if (Files.isDirectory(path))
        directoryContents(path).foreach(deletePath)
      else
        Files.delete(path)
    }
  }

  /**
   * Copies the contents inside one directory into another directory.
   *
   * @param inputDir The directory whose contents to copy
   * @param outputDir The destination for the copied content
   * @param copyRoot If true, copies the input directory instead of
   *                 just all of the content inside
   */
  private def copyDirectoryContents(
    inputDir: Path,
    outputDir: Path,
    copyRoot: Boolean = false
  ): Unit = {
    val rootDir = inputDir

    def copyContents(inputPath: Path, outputDir: Path): Unit = {
      val relativeInputPath = rootDir.relativize(inputPath)
      val outputPath = outputDir.resolve(relativeInputPath)

      if (!Files.isDirectory(inputPath)) Files.copy(inputPath, outputPath)
      else {
        Files.createDirectories(outputPath)
        directoryContents(inputPath).foreach(p => copyContents(p, outputDir))
      }
    }

    if (Files.isDirectory(inputDir)) {
      directoryContents(inputDir).foreach(p => copyContents(p, outputDir))
    } else {
      copyContents(inputDir, outputDir)
    }
  }
}
