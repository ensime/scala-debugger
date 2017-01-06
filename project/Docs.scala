import sbt.Keys._
import sbt._

object Docs {
  lazy val unfilteredVersion: SettingKey[String] = settingKey[String](
    "Version of Unfiltered used in projects"
  )

  /** Docs-specific project settings. */
  val settings = Seq(
    // Contains the version of unfiltered used
    unfilteredVersion := "0.9.0-beta2",

    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "scalatags" % "0.6.2", // Main doc generator
      "org.rogach" %% "scallop" % "2.0.5", // CLI Support

      "com.vladsch.flexmark" % "flexmark-java" % "0.10.3", // Markdown support
      "com.vladsch.flexmark" % "flexmark-ext-yaml-front-matter" % "0.10.3", // Front matter support
      "com.vladsch.flexmark" % "flexmark-ext-gfm-tables" % "0.10.3", // Tables support
      "com.vladsch.flexmark" % "flexmark-ext-abbreviation" % "0.10.3", // Abbreviation support

      "commons-codec" % "commons-codec" % "1.10", // Base64 encoding support

      // For hosting local server containing generated sources
      "ws.unfiltered" %% "unfiltered" % unfilteredVersion.value,
      "ws.unfiltered" %% "unfiltered-filter" % unfilteredVersion.value,
      "ws.unfiltered" %% "unfiltered-jetty" % unfilteredVersion.value
    )
  )
}
