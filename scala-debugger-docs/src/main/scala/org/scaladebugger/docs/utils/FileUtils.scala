package org.scaladebugger.docs.utils

import java.nio.file.{FileSystems, Files, Path}

import scala.annotation.tailrec

/**
 * Contains a collection of utility functions regarding
 * paths and files.
 */
object FileUtils {
  /** Represents a matcher for markdown paths. */
  private lazy val MarkdownMatcher =
    FileSystems.getDefault.getPathMatcher("glob:**.md")

  /**
   * Creates an empty file at the specified path.
   *
   * @param path The path to the file
   * @return The resulting path
   */
  def createEmptyFile(path: Path): Path = Files.createFile(path)

  /**
   * Retrieves all markdown files found in the specified
   * directories or any of their subdirectories.
   *
   * @param paths The paths to the directories to traverse
   * @return An iterable collection of paths to markdown files
   */
  @tailrec def markdownFiles(paths: Path*): Iterable[Path] = {
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
  def directoryContents(path: Path): Iterable[Path] = {
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
  def directories(
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
  def deletePath(path: Path): Unit = {
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
  def copyDirectoryContents(
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

  /**
   * Removes the file extension from the name.
   *
   * @param fileName The file name whose extension to remove
   * @return The file name without the extension
   */
  def stripExtension(fileName: String): String = {
    fileName.replaceFirst("[.][^.]+$", "")
  }
}
