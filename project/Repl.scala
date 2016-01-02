import sbt.Keys._
import sbt._

object Repl {
  /** REPL-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "com.lihaoyi" % "ammonite-repl" % "0.5.2" cross CrossVersion.full,
      "com.lihaoyi" %% "ammonite-terminal" % "0.5.2",
      "org.parboiled" %% "parboiled" % "2.1.0",
      "org.slf4j" % "slf4j-api" % "1.7.5",
      "org.slf4j" % "slf4j-log4j12" % "1.7.5" % "test,it",
      "log4j" % "log4j" % "1.2.17" % "test,it",
      "org.scalatest" %% "scalatest" % "3.0.0-M14" % "test,it",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2.1" % "test,it"
    )
  )
}
