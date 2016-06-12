import sbt.Keys._
import sbt._

object Language {
  /** Language-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "org.parboiled" %% "parboiled" % "2.1.0",
      "org.scalatest" %% "scalatest" % "3.0.0-M16-SNAP1" % "test,it",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.2.1" % "test,it"
    )
  )
}
