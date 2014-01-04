import AssemblyKeys._

net.virtualvoid.sbt.graph.Plugin.graphSettings

// Basic options

name := "auginte"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")


// Dependencies

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" % "akka-actor_2.10" % "2.2.0"

libraryDependencies += "org.scalatest" % "scalatest_2.10" % "1.9.1" % "test"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies += "org.scalacheck" %% "scalacheck" % "1.10.1" % "test" intransitive()

assemblySettings


// ScalaDoc

scalacOptions in(Compile, doc) ++= Opts.doc.title("Scalidea documentation")

scalacOptions in(Compile, doc) += "-diagrams"

scalacOptions in(Compile, doc) += "-implicits"

scalacOptions in(Compile, doc) += "-groups"

// Idea

ideaExcludeFolders += ".idea"

ideaExcludeFolders += ".idea_modules"

// com.auginte.Main gui

mainClass in(Compile, run) := Some("com.auginte.main.Main")
