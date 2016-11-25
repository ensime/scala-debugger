package org.scaladebugger.api.utils

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file._

import FileSearcher._

/**
 * Exposes utility methods related to searching files.
 */
object FileSearcher extends FileSearcher {
  /** Represents the default search depth (unlimited). */
  val DefaultSearchDepth = Int.MaxValue
}

/**
 * Contains utility methods related to searching files.
 */
class FileSearcher private[utils] {
  /**
   * Returns a new path matcher using glob system from the
   * provided glob string.
   *
   * @example newGlobMatcher("*.{java,class}")
   *
   * @param globString The glob string to use in constructing the matcher
   * @return The new path matcher for glob-based matching
   */
  def newGlobMatcher(globString: String): PathMatcher =
    FileSystems.getDefault.getPathMatcher(s"glob:$globString")

  /**
   * Loads all files recursively at a specified path and returns a mapping
   * of file name (e.g. myFile.txt) to a collection of paths that point to
   * files with that name. Searches infinite directories recursively for files.
   *
   * @param rootPath The root path to start checking files
   * @return Collection of file paths
   * @throws IOException If an error is encountered when loading file paths
   */
  def loadFilePaths(rootPath: Path): Seq[Path] = {
    loadFilePaths(
      rootPath = rootPath,
      pathMatcher = None,
      maxDepth = DefaultSearchDepth
    )
  }

  /**
   * Loads all files recursively at a specified path and returns a mapping
   * of file name (e.g. myFile.txt) to a collection of paths that point to
   * files with that name.
   *
   * @param rootPath The root path to start checking files
   * @param maxDepth The maximum depth to traverse with 0 being only the
   *                 root file itself
   * @return Collection of file paths
   * @throws IOException If an error is encountered when loading file paths
   */
  def loadFilePaths(
    rootPath: Path,
    maxDepth: Int
  ): Seq[Path] = loadFilePaths(
    rootPath = rootPath,
    pathMatcher = None,
    maxDepth = maxDepth
  )

  /**
   * Loads all files recursively at a specified path and returns a mapping
   * of file name (e.g. myFile.txt) to a collection of paths that point to
   * files with that name. Searches infinite directories recursively for files.
   *
   * @param rootPath The root path to start checking files
   * @param globString The string to use as a glob path matcher when
   *                   determining which files to include
   * @return Collection of file paths
   * @throws IOException If an error is encountered when loading file paths
   */
  def loadFilePaths(
    rootPath: Path,
    globString: String
  ): Seq[Path] = {
    loadFilePaths(
      rootPath = rootPath,
      pathMatcher = Some(newGlobMatcher(globString)),
      maxDepth = DefaultSearchDepth
    )
  }

  /**
   * Loads all files recursively at a specified path and returns a mapping
   * of file name (e.g. myFile.txt) to a collection of paths that point to
   * files with that name.
   *
   * @param rootPath The root path to start checking files
   * @param globString The string to use as a glob path matcher when
   *                   determining which files to include
   * @param maxDepth The maximum depth to traverse with 0 being only the
   *                 root file itself
   * @return Collection of file paths
   * @throws IOException If an error is encountered when loading file paths
   */
  def loadFilePaths(
    rootPath: Path,
    globString: String,
    maxDepth: Int
  ): Seq[Path] = {
    loadFilePaths(
      rootPath = rootPath,
      pathMatcher = Some(newGlobMatcher(globString)),
      maxDepth = maxDepth
    )
  }

  /**
   * Loads all files recursively at a specified path and returns a mapping
   * of file name (e.g. myFile.txt) to a collection of paths that point to
   * files with that name. Searches infinite directories recursively for files.
   *
   * @param rootPath The root path to start checking files
   * @param pathMatcher If provided, will restrict loaded files to only those
   *                    that are accepted by the path matcher
   * @return Collection of file paths
   * @throws IOException If an error is encountered when loading file paths
   */
  @throws[IOException]
  def loadFilePaths(
    rootPath: Path,
    pathMatcher: Option[PathMatcher]
  ): Seq[Path] = loadFilePaths(
    rootPath = rootPath,
    pathMatcher = pathMatcher,
    maxDepth = DefaultSearchDepth
  )

  /**
   * Loads all files recursively at a specified path and returns a mapping
   * of file name (e.g. myFile.txt) to a collection of paths that point to
   * files with that name.
   *
   * @param rootPath The root path to start checking files
   * @param pathMatcher If provided, will restrict loaded files to only those
   *                    that are accepted by the path matcher
   * @param maxDepth The maximum depth to traverse with 0 being only the
   *                 root file itself
   * @return Collection of file paths
   * @throws IOException If an error is encountered when loading file paths
   */
  @throws[IOException]
  def loadFilePaths(
    rootPath: Path,
    pathMatcher: Option[PathMatcher],
    maxDepth: Int
  ): Seq[Path] = {
    // Create an empty internal map that we will populate
    import scala.collection.mutable.{Buffer => mutBuffer}
    val internalPaths = mutBuffer[Path]()

    // Traverses the file tree, adding each file to our mapping
    val simpleFileVisitor = new SimpleFileVisitor[Path] {
      override def visitFile(
        file: Path,
        attrs: BasicFileAttributes
      ): FileVisitResult = {
        // Check if we should add this file to our collection
        // and ignore non-regular files (like directories)
        val shouldAdd = pathMatcher match {
          case Some(matcher)  => matcher.matches(file)
          case None           => true
        }
        if (shouldAdd && attrs.isRegularFile)
          internalPaths += file

        FileVisitResult.CONTINUE
      }
    }

    // Provide no options for now
    import scala.collection.JavaConverters._
    val visitOptions = Set[FileVisitOption]().asJava

    // Walk the tree
    Files.walkFileTree(
      rootPath,
      visitOptions,
      maxDepth,
      simpleFileVisitor
    )

    internalPaths
  }
}
