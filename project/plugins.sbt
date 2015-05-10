resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.7.0-SNAPSHOT")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.0")

resolvers += Classpaths.typesafeReleases

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")

addSbtPlugin("com.orrsella" % "sbt-stats" % "1.0.5")

addSbtPlugin("org.xerial.sbt" % "sbt-pack" % "0.6.2")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.2")

// Comment to get more information during initialization
logLevel := Level.Warn

// Sbt plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.7")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.2.4")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")