import sbt._
import Keys._

import scala.util.Try


object Common {
  lazy val scalaTestSpanScaleFactor = settingKey[Double](
    "Sets scaling factor of running tests that are wrapped in scale(...)"
  )

  def settings = Seq(
    version := "1.1.0-SNAPSHOT",

    organization := "org.scala-debugger",

    licenses += (
      "Apache-2.0",
      url("https://www.apache.org/licenses/LICENSE-2.0.html")
    ),

    homepage := Some(url("https://scala-debugger.org")),

    // Default version when not cross-compiling
    scalaVersion := "2.10.6",

    crossScalaVersions := Seq("2.10.6", "2.11.8"),

    scalacOptions ++= Seq(
      "-encoding", "UTF-8", "-target:jvm-1.6",
      "-deprecation", "-unchecked", "-feature",
      "-Xfatal-warnings"
    ) ++ (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, scalaMajor)) if scalaMajor == 10 => Seq("-Ywarn-all")
      case _ => Nil
    }),

    javacOptions ++= Seq(
      "-source", "1.6", "-target", "1.6", "-Xlint:all", "-Werror",
      "-Xlint:-options", "-Xlint:-path", "-Xlint:-processing"
    ),
  
    scalacOptions in (Compile, doc) ++= Seq(
      "-no-link-warnings" // Suppress problems with Scaladoc @throws links
    ),

    // Options provided to forked JVMs through sbt, based on our .jvmopts file
    javaOptions ++= Seq(
      "-Xms1024M", "-Xmx4096M", "-Xss2m", "-XX:MaxPermSize=1024M",
      "-XX:ReservedCodeCacheSize=256M", "-XX:+TieredCompilation",
      "-XX:+CMSPermGenSweepingEnabled", "-XX:+CMSClassUnloadingEnabled",
      "-XX:+UseConcMarkSweepGC", "-XX:+HeapDumpOnOutOfMemoryError"
    ),

    scalaTestSpanScaleFactor := {
      Try(System.getenv("SCALATEST_SPAN_SCALE_FACTOR").toDouble).getOrElse(1.0)
    },

    testOptions in Test += Tests.Argument("-oDF"),

    testOptions in IntegrationTest += Tests.Argument("-oDF"),

    testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest,
      "-F", scalaTestSpanScaleFactor.value.toString
    ),

    testOptions in IntegrationTest += Tests.Argument(TestFrameworks.ScalaTest,
      "-F", scalaTestSpanScaleFactor.value.toString
    ),

    // Run tests in parallel
    // NOTE: Needed to avoid ScalaTest serialization issues
    parallelExecution in Test := true,
    testForkedParallel in Test := true,

    // Run integration tests in parallel
    parallelExecution in IntegrationTest := true,
    testForkedParallel in IntegrationTest := true,


    // Properly handle Scaladoc mappings
    autoAPIMappings := true,

    // Prevent publishing test artifacts
    publishArtifact in Test := false,

    publishMavenStyle := true,

    pomExtra :=
      <scm>
        <url>git@github.com:ensime/scala-debugger.git</url>
        <connection>scm:git:git@github.com:ensime/scala-debugger.git</connection>
      </scm>
      <developers>
        <developer>
          <id>senkwich</id>
          <name>Chip Senkbeil</name>
          <url>https://www.chipsenkbeil.org</url>
        </developer>
      </developers>,

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  ) ++ Macros.pluginSettings
}

