import sbt.Keys._

object SbtPlugin {
  /** Sbt plugin-specific project settings. */
  val settings = Seq(
    sbtPlugin := true,
    scalaVersion := "2.10.6",
    crossScalaVersions := Seq("2.10.6")
  )
}
