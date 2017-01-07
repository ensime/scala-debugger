package org.scaladebugger.docs

import java.nio.file._

import org.scaladebugger.docs.layouts.Context
import org.scaladebugger.docs.structures.{MenuItem, Page}
import org.scaladebugger.docs.utils.FileUtils

/**
 * Represents a generator of content based on a configuration.
 *
 * @param config The configuration to use when generating files
 */
class Generator(private val config: Config) {
  /** Logger for this class. */
  private lazy val logger = new Logger(this.getClass)

  /** Used for Github pages. */
  private val NoJekyllFile = ".nojekyll"

  /**
   * Runs the generator.
   */
  def run(): Unit = logger.time(Logger.Level.Info, "Gen finished after ") {
    val outputDir = config.outputDir()

    val inputDir = config.inputDir()
    val srcDir = config.srcDir()
    val staticDir = config.staticDir()

    val outputDirPath = Paths.get(outputDir)
    outputDirPath.getFileName

    // Re-create the output directory
    logger.trace(s"Deleting and recreating $outputDirPath")
    FileUtils.deletePath(outputDirPath)
    Files.createDirectories(outputDirPath)

    // Copy all static content
    val staticDirPath = Paths.get(inputDir, staticDir)
    logger.trace(s"Copying static files from $staticDirPath to $outputDirPath")
    FileUtils.copyDirectoryContents(staticDirPath, outputDirPath)

    // Generate .nojekyll file
    if (config.doNotGenerateNoJekyllFile()) {
      logger.trace(s"Not generating $NoJekyllFile")
    } else {
      logger.trace(s"Generating $NoJekyllFile")
      val noJekyllFilePath = outputDirPath.resolve(NoJekyllFile)
      FileUtils.createEmptyFile(noJekyllFilePath)
    }

    // Process all markdown files
    val srcDirPath = Paths.get(inputDir, srcDir)
    logger.trace(s"Processing markdown files from $srcDirPath")

    val linkedMainMenuItems = MenuItem.fromPath(
      config,
      srcDirPath,
      dirUseFirstChild = true
    ).map(_.copy(children = Nil))
    val linkedSideMenuItems = MenuItem.fromPath(config, srcDirPath)

    // Create our layout context
    val context = Context(
      mainMenuItems = linkedMainMenuItems,
      sideMenuItems = linkedSideMenuItems
    )

    // For each markdown file, generate its content and produce a file
    val mdFiles = FileUtils.markdownFiles(srcDirPath)
    mdFiles.map(f => Page.Session.newInstance(config, f)).foreach(page => {
      page.render(context.copy(title = Some(page.title)))
    })
  }
}
