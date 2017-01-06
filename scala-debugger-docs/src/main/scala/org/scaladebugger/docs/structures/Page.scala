package org.scaladebugger.docs.structures

import java.nio.charset.Charset
import java.nio.file.{Files, Path, Paths, StandardOpenOption}
import java.util.concurrent.TimeUnit

import com.vladsch.flexmark.ast.Node
import com.vladsch.flexmark.ext.abbreviation.AbbreviationExtension
import com.vladsch.flexmark.ext.front.matter.{AbstractYamlFrontMatterVisitor, YamlFrontMatterExtension}
import com.vladsch.flexmark.ext.gfm.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import org.scaladebugger.docs.{Config, FileUtils, Logger}
import org.scaladebugger.docs.layouts.{Context, Layout}

import scala.util.Try

/**
 * Represents a page of content.
 *
 * @param config Used to fill in defaults
 * @param path The path to the raw page content
 * @param logger The logger to use with the page
 */
class Page private (
  val config: Config,
  val path: Path
)(
  private val logger: Logger = new Logger(classOf[Page]).newSession(
    path.toString
  ).init()
) {
  /** Represents an internal flexmark node used for markdown processing. */
  private lazy val pageNode = parseMarkdownFile(path)

  /** Represents the metadata for the page. */
  lazy val metadata: Metadata = parseMetadata(pageNode)

  /** Represents an absolute web path link to this file. */
  lazy val absoluteLink: String = {
    val srcDirPath = {
      val inputDir = config.inputDir()
      val srcDir = config.srcDir()
      Paths.get(inputDir, srcDir)
    }

    "/" + FileUtils.stripExtension(srcDirPath.relativize(path).toString)
      .replaceAllLiterally(java.io.File.separator, "/") + "/"
  }

  /** Represents the name of the page. */
  lazy val name: String = {
    FileUtils.stripExtension(
      path.getFileName.toString
    ).replaceAll("[^\\w\\s\\d]", " ")
  }

  /**
   * Returns whether or not the page is using the default layout.
   *
   * @return True if using the default layout, otherwise false
   */
  def isUsingDefaultLayout: Boolean = metadata.usingDefaultLayout

  /**
   * Renders the page and writes it to the output path.
   *
   * @param context The context to feed into this page's layout
   * @param path The path to render the file, defaulting to the
   *             page's standard output path
   * @return True if successfully rendered the page, otherwise false
   */
  def render(context: Context, path: Path = outputPath): Boolean = {
    val startTime = System.nanoTime()

    val result = Try(writeText(path, renderToString(context)))

    result.failed.foreach(t => {
      val errorName = t.getClass.getName
      val errorMessage = Option(t.getLocalizedMessage).getOrElse("<none>")
      val depth = config.stackTraceDepth()
      val stackTrace =
        if (depth < 0) t.getStackTrace
        else t.getStackTrace.take(depth)

      logger.error(s"!!! Failed: " + errorName)
      logger.error(s"!!! Message: " + errorMessage)
      stackTrace.foreach(ste => logger.error(s"!!! $ste"))
    })

    val endTime = System.nanoTime()

    // Nano is 10^-9
    val timeTaken = endTime - startTime
    val timeTakenMillis = TimeUnit.NANOSECONDS.toMillis(timeTaken)
    val timeTakenNanosRemainder =
      timeTaken - TimeUnit.MILLISECONDS.toNanos(timeTakenMillis)
    logger.log(s"Took $timeTakenMillis.${timeTakenNanosRemainder}ms")

    result.isSuccess
  }

  /** Represents the output path when the page is rendered. */
  lazy val outputPath: Path = {
    val srcDirPath = {
      val inputDir = config.inputDir()
      val srcDir = config.srcDir()
      Paths.get(inputDir, srcDir)
    }

    val outputDirPath = {
      val outputDir = config.outputDir()
      Paths.get(outputDir)
    }

    val relativePath = srcDirPath.relativize(path)
    val fileName = FileUtils.stripExtension(path.getFileName.toString)

    // Create the output path to the new html file's directory
    val outputPath = Option(relativePath.getParent)
      .map(outputDirPath.resolve)
      .getOrElse(outputDirPath)
    val htmlDirPath =
      if (isIndexPage) outputPath
      else outputPath.resolve(fileName)
    Files.createDirectories(htmlDirPath)

    // Create the path to the html file itself
    htmlDirPath.resolve("index.html")
  }

  /**
   * Represents whether or not this page represents an index page.
   */
  lazy val isIndexPage: Boolean = {
    Files.isRegularFile(path) &&
      FileUtils.stripExtension(path.getFileName.toString) == "index"
  }

  /**
   * Renders the page as a string.
   *
   * @param context The context to feed into this page's layout
   * @return The textual representation of the rendering
   */
  private def renderToString(context: Context): String = {
    val layout = newLayoutInstance(context)

    logger.log(s"Rendering markdown as html")
    val content = Page.renderer.render(pageNode)

    logger.log(s"Applying layout ${layout.getClass.getName}")
    layout.toString(content)
  }

  /**
   * Generates a new instance of this page's layout using the provided context.
   *
   * @param context The context to feed into this page's layout
   * @return The new layout instance
   */
  private def newLayoutInstance(context: Context): Layout = {
    if (metadata.usingDefaultLayout) {
      logger.log(s"Loading default layout")
      defaultLayout(context)
    } else {
      val layoutName = metadata.layout
      logger.log(s"Loading layout $layoutName")
      layoutFromClassName(layoutName, context)
    }
  }

  /**
   * Retrieves the default layout.
   *
   * @param context The context to provide to the default layout
   * @return The default layout instance
   */
  private def defaultLayout(context: Context): Layout = {
    val defaultLayoutClassName = config.defaultPageLayout()
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
   * Parses a file at the specified path as flexmark node.
   *
   * @param path The path to the markdown file
   * @return The new flexmark node instance
   */
  private def parseMarkdownFile(path: Path): Node = {
    logger.log(s"Parsing markdown file")
    Page.parser.parseReader(
      Files.newBufferedReader(path, Charset.forName("UTF-8"))
    )
  }

  /**
   * Parses the metadata from the specified flexmark node.
   *
   * @param node The flexmark node whose metadata to parse
   * @return The metadata instance
   */
  private def parseMetadata(node: Node): Metadata = {
    logger.log(s"Extracting front matter from markdown file")
    val yamlVisitor = new AbstractYamlFrontMatterVisitor
    yamlVisitor.visit(node)
    Metadata.fromJavaMap(config, yamlVisitor.getData)
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
}

object Page {
  import scala.collection.JavaConverters._

  /** Represents extensions for the parser and renderer. */
  private lazy val extensions = Seq(
    AbbreviationExtension.create(),
    TablesExtension.create(),
    YamlFrontMatterExtension.create()
  ).asJava

  /** Represents the Markdown parser. */
  private lazy val parser = Parser.builder().extensions(extensions).build()

  /** Represents the Markdown => HTML renderer. */
  private lazy val renderer =
    HtmlRenderer.builder().extensions(extensions).build()

  def newInstance(
    config: Config,
    path: Path
  ): Page = new Page(config, path)()

  def newInstance(
    config: Config,
    path: Path,
    logger: Logger
  ): Page = new Page(config, path)(logger)
}
