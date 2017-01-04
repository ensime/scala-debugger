logLevel := Level.Warn

resolvers += Resolver.sonatypeRepo("snapshots")

resolvers += Resolver.sonatypeRepo("releases")

// Used to add tools.jar to sbt classpath
addSbtPlugin("org.scala-debugger" % "sbt-jdi-tools" % "1.0.0")

// Used for building fat jars
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.1")

// Use to respect cross-compilation settings
addSbtPlugin("com.eed3si9n" % "sbt-doge" % "0.1.5")

