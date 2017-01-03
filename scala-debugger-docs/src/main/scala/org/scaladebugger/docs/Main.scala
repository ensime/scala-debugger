package org.scaladebugger.docs

import java.nio.file._

import org.scaladebugger.docs.layouts.FrontPage

import scala.util.Try

/**
 * Main entrypoint for generating and serving docs.
 */
object Main {
  private def log(text: String): Unit = {
    import java.util.Calendar
    import java.text.SimpleDateFormat
    val format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS")
    val timestamp = format.format(Calendar.getInstance().getTime)
    println(s"[$timestamp] $text")
  }

  private def directoryContents(path: Path): Iterable[Path] = {
    import scala.collection.JavaConverters._
    Files.newDirectoryStream(path).asScala
  }

  private def deleteDirectories(path: Path): Unit = {
    if (Files.exists(path)) {
      if (Files.isDirectory(path))
        directoryContents(path).foreach(deleteDirectories)
      else
        Files.delete(path)
    }
  }

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

  def main(args: Array[String]): Unit = {
    val config = new Config(args)

    val outputDir = config.outputDir()
    val indexFiles = config.indexFiles()

    val inputDir = config.inputDir()
    val srcDir = config.srcDir()
    val staticDir = config.staticDir()

    // Generate before other actions if indicated
    if (config.generate()) {
      val outputDirPath = Paths.get(outputDir)

      // Re-create the output directory
      deleteDirectories(outputDirPath)
      Files.createDirectories(outputDirPath)

      // Copy all static content
      val staticDirPath = Paths.get(inputDir, staticDir)
      copyDirectoryContents(staticDirPath, outputDirPath)

      // Process all markdown files
      /*
        val mdMatcher = FileSystems.getDefault.getPathMatcher("glob:*.md")
        if (mdMatcher.matches(mdPath)) // Do something
       */

      // TODO: Create auto generator of content
      // Create front page
      val frontPageText = FrontPage().toString
      val frontPagePath = Paths.get(outputDir, "index.html")
      Files.write(
        frontPagePath,
        frontPageText.getBytes("UTF-8"),
        StandardOpenOption.CREATE,
        StandardOpenOption.WRITE
      )
    }

    if (config.serve()) {
      import unfiltered.request.{Path => UFPath, _}
      import unfiltered.response._
      val hostedContent = unfiltered.filter.Planify {
        case GET(UFPath(path)) =>
          val rawPath = Paths.get(outputDir, path)
          val indexPaths = indexFiles.map(f => Paths.get(outputDir, path, f))
          val fileBytes = (rawPath +: indexPaths).filter(p =>
            Try(Files.exists(p)).getOrElse(false)
          ).map(p =>
            (p, Try(Files.readAllBytes(p)))
          ).filter(_._2.isSuccess).map(t => (t._1, t._2.get)).headOption
          fileBytes match {
            case Some(result) =>
              val (filePath, fileBytes) = result

              log(s"GET '$path' -> $filePath")
              ResponseBytes(fileBytes)
            case None => ResponseString(s"Unknown page: $path")
          }
        case _ =>
          ResponseString("Unknown request")
      }
      unfiltered.jetty.Server.http(8080).plan(hostedContent).run()
    } else if (config.publish()) {
      // TODO: Implement publish using Scala process to run git
    } else {
      config.printHelp()
    }
  }
}
