import sbt.Keys._

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "2.0" % "test"

libraryDependencies += "org.scalautils" % "scalautils_2.10" % "2.0" % "test"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "org.scala-lang" % "scala-actors" % "2.10.0"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.0"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.1" % "test" intransitive()
