import sbt.Keys._

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "org.scala-lang" % "scala-actors" % "2.11.2"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.2"
