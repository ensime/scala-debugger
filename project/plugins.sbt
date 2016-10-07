logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

// Used for formatting source to certain style
addSbtPlugin("com.typesafe.sbt" % "sbt-scalariform" % "1.3.0")

// Used for style notes
addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.6.0")

// Used for signing in order to publish jars
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// Used to ensure proper publish process is followed
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.5.0")

// Used to provide unified documentation across modules
addSbtPlugin("com.eed3si9n" % "sbt-unidoc" % "0.3.3")

// Used to add tools.jar to sbt classpath
addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0")

// Used for building fat jars
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.1")

// Used for better dependency resolution and downloading
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M14-3")

