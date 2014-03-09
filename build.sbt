import AssemblyKeys._

net.virtualvoid.sbt.graph.Plugin.graphSettings

// Basic options

name := "auginte"

scalaVersion := "2.10.2"

scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature")

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
