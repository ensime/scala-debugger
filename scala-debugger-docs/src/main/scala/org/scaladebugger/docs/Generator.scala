package org.scaladebugger.docs

import java.nio.file._

import org.scaladebugger.docs.layouts.Context
import org.scaladebugger.docs.layouts.partials.common.MenuItem
import org.scaladebugger.docs.structures.Page

/**
 * Represents a generator of content based on a configuration.
 *
 * @param config The configuration to use when generating files
 */
class Generator(private val config: Config) {
  /** Logger for this class. */
  private val logger = new Logger(this.getClass)

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
    FileUtils.deletePath(outputDirPath)
    Files.createDirectories(outputDirPath)

    // Copy all static content
    val staticDirPath = Paths.get(inputDir, staticDir)
    FileUtils.copyDirectoryContents(staticDirPath, outputDirPath)

    // Process all markdown files
    val srcDirPath = Paths.get(inputDir, srcDir)
    val mdFiles = FileUtils.markdownFiles(srcDirPath)

    // Find all directories of src dir
    val directoryPaths = FileUtils.directories(srcDirPath)

    // Generate top-level menu items based on src dir
    def createLinkedMenuItem(
      allPaths: Seq[Path],
      path: Path,
      dirUseFirstChild: Boolean
    ): MenuItem = {
      val children = allPaths.filter(_.getParent == path).map(p =>
        createLinkedMenuItem(allPaths, p, dirUseFirstChild))

      val page = Page.newInstance(config, path)

      // Directories use first child as link
      val isDir = Files.isDirectory(path)
      val link =
        if (isDir && dirUseFirstChild)
          children.find(_.link.nonEmpty).flatMap(_.link)
        else if (isDir && !dirUseFirstChild)
          None
        else
          Some(page.absoluteLink)

      MenuItem(
        name = page.name,
        link = link,
        children = children
      )
    }

    // All paths excluding top-level index.md
    val allPaths: Seq[Path] = (mdFiles ++ directoryPaths)
      .filterNot(p => srcDirPath.relativize(p) == Paths.get("index.md")).toSeq
    val linkedMainMenuItems = allPaths
      .filter(_.getParent == srcDirPath)
      .map(p => createLinkedMenuItem(allPaths, p, dirUseFirstChild = true))
      .map(_.copy(children = Nil))
    val linkedSideMenuItems = allPaths
      .filter(_.getParent == srcDirPath)
      .map(p => createLinkedMenuItem(allPaths, p, dirUseFirstChild = false))

    // Create our layout context
    val context = Context(
      mainMenuItems = linkedMainMenuItems,
      sideMenuItems = linkedSideMenuItems
    )

    // For each markdown file, generate its content and produce a file
    mdFiles.foreach(mdFile => Page.newInstance(config, mdFile).render(context))
  }
}
