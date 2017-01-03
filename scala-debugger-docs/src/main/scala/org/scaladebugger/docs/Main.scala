package org.scaladebugger.docs

import java.nio.file.{Files, Path, Paths, StandardOpenOption}

import org.scaladebugger.docs.layouts.FrontPage

import scala.util.Try

/**
 * Main entrypoint for generating and serving docs.
 */
object Main {
  private def deleteDirectories(path: Path): Unit = {
    import scala.collection.JavaConverters._
    if (Files.exists(path)) {
      if (Files.isDirectory(path))
        Files.newDirectoryStream(path).asScala.foreach(deleteDirectories)
      else
        Files.delete(path)
    }
  }

  def main(args: Array[String]): Unit = {
    val config = new Config(args)
    val outputDir = config.outputDir()
    val indexFiles = config.indexFiles()

    // Generate before other actions if indicated
    if (config.generate()) {
      val outputDirPath = Paths.get(outputDir)

      // Re-create the output directory
      deleteDirectories(outputDirPath)
      Files.createDirectories(outputDirPath)

      // Copy all static content
      // TODO: Implement by providing an input directory with
      //       subdirectories of 'src' and 'static' where static
      //       files are copied directly and src files are
      //       transformed (parse markdown, run through template, etc)

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
            Try(Files.readAllBytes(p))
          ).filter(_.isSuccess).map(_.get).headOption
          fileBytes match {
            case Some(bytes)  => ResponseBytes(bytes)
            case None         => ResponseString(s"Unknown page: $path")
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
