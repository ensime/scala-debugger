import sbt.Keys._
import sbt._

object Repl {
  /** REPL-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "com.lihaoyi" % "ammonite-repl" % "0.5.2" cross CrossVersion.full,
      "com.lihaoyi" %% "ammonite-terminal" % "0.5.2",
      "org.parboiled" %% "parboiled" % "2.1.0"
    )
  )
}
