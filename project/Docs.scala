import sbt.Keys._
import sbt._

object Docs {
  /** Docs-specific project settings. */
  val settings = Seq(
    libraryDependencies ++= Seq(
      "org.senkbeil" %% "site-generator-layouts" % "0.1.2"
    )
  )
}
