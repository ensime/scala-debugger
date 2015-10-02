import sbt._
import Keys._


object Common {
  def settings = Seq(
    version := "1.0.0",

    organization := "org.senkbeil",

    licenses += (
      "Apache-2.0",
      url("https://www.apache.org/licenses/LICENSE-2.0.html")
    ),

    homepage := Some(url("http://www.senkbeil.org/")),

    // Default version when not cross-compiling
    scalaVersion := "2.10.5",

    crossScalaVersions := Seq("2.10.5", "2.11.6"),

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

    testOptions in Test += Tests.Argument("-oDF"),

    // Properly handle Scaladoc mappings
    autoAPIMappings := true,

    // Prevent publishing test artifacts
    publishArtifact in Test := false,

    publishMavenStyle := true,

    pomExtra :=
      <scm>
        <url>git@github.com:chipsenkbeil/scala-debugger.git</url>
        <connection>scm:git:git@github.com:chipsenkbeil/scala-debugger.git</connection>
      </scm>
      <developers>
        <developer>
          <id>senkwich</id>
          <name>Chip Senkbeil</name>
          <url>http://www.senkbeil.org</url>
        </developer>
      </developers>,

    publishTo := {
      val nexus = "https://oss.sonatype.org/"
      if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    }
  )
}

