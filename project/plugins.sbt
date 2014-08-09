resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.7.0-SNAPSHOT")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.0")

resolvers += Classpaths.typesafeReleases

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.7.4")
