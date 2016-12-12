import sbt.Keys._
import sbt._

object Docs {
  /** Docs-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.6.2", // Main doc generator
      "org.rogach" %% "scallop" % "2.0.5", // CLI Support
      "org.scalatra" %% "scalatra" % "2.5.0" // Running local webserver
    )
  )
}
