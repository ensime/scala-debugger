import sbt.Keys._
import sbt._

object Language {
  /** Language-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "org.parboiled" %% "parboiled" % "2.1.0",
      "org.scalatest" %% "scalatest" % "3.0.0" % "test,it",
      "org.scalamock" %% "scalamock-scalatest-support" % "3.4.2" % "test,it"
    )
  )
}
