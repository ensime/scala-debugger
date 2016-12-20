name := "sbt-scala-debugger"

sbtPlugin := true

organization := "org.scala-debugger"

scalacOptions += "-target:jvm-1.7"

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

licenses := Seq(
  "Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0.html")
)

homepage := Some(url("https://scala-debugger.org"))

pomExtra := {
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
    <developer>
      <id>dickwall</id>
      <name>Dick Wall</name>
      <url>http://escalatesoft.com</url>
    </developer>
  </developers>
}

credentials += {
  Seq("SONATYPE_USER", "SONATYPE_PASS").map(sys.env.get) match {
    case Seq(Some(user), Some(pass)) =>
      Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
    case _ =>
      Credentials(Path.userHome / ".ivy2" / ".credentials")
  }
}

releaseSettings

ReleaseKeys.versionBump := sbtrelease.Version.Bump.Bugfix

sbtrelease.ReleasePlugin.ReleaseKeys.publishArtifactsAction := PgpKeys.publishSigned.value
