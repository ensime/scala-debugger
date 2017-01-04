package org.scaladebugger.docs

import java.nio.file._

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import org.scaladebugger.docs.layouts.partials.common.MenuItem
import org.scaladebugger.docs.layouts.{Context, DocPage, FrontPage, Layout}

import scala.annotation.tailrec
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
    val linkedMenuItems = directoryPaths.filter(_.getParent == srcDirPath)
      .map(createLinkedMenuItem)

    // Create our layout context
    val context = Context(
      mainMenuItems = linkedMenuItems,
      sideMenuItems = linkedMenuItems
    )

    // Set up markdown parser and renderer
    val parser = Parser.builder().build()
    val renderer = HtmlRenderer.builder().build()

    // For each markdown file, generate its content and produce a file
    mdFiles.foreach(mdFile => Try {
      val relativePath = srcDirPath.relativize(mdFile)
      val fileName = mdFile.getFileName.toString.replaceFirst("[.][^.]+$", "")

      // Create the output path to the new html file's directory
      val outputPath = Option(relativePath.getParent)
        .map(outputDirPath.resolve)
        .getOrElse(outputDirPath)
      val htmlDirPath = outputPath.resolve(fileName)
      Files.createDirectories(htmlDirPath)

      // Create the path to the html file itself
      val htmlFilePath = htmlDirPath.resolve("index.html")

      // Parse the md file into a node
      logger.log(s"Parsing ${mdFile.toString}")
      val markdownDocument = parser.parseReader(
        Files.newBufferedReader(mdFile)
      )

      // Render markdown content to html
      logger.log(s"Rendering ${mdFile.toString}")
      val markdownContent = renderer.render(markdownDocument)

      // Load layout for file
      // TODO: Parse correct layout to use
      val layout = defaultLayout(context)

      // Apply associated layout
      logger.log(s"Applying layout ${layout.getClass.getName} to ${mdFile.toString}")
      val htmlDocumentContent = layout.toString(markdownContent)

      // Write the md file as html to a file
      logger.log(s"Writing to ${htmlFilePath.toString}")
      writeText(htmlFilePath, htmlDocumentContent)
    }.failed.foreach(logger.error))

    // TODO: Create auto generator of content
    // Create front page
    val frontPage = new FrontPage
    frontPage.context = context
    val frontPageText = frontPage.toString
    val frontPagePath = Paths.get(outputDir, "index.html")
    writeText(frontPagePath, frontPageText)
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
    var directories = mutable.Buffer[Path]()

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
