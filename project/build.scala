import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object AuginteBuild extends sbt.Build {
  val buildName = "auginte"
  val buildOrganization = "com.autinte"
  val buildVersion      = "0.5.0-SNAPSHOT"
  val buildScalaVersion = "2.10.3"
  val buildMainClass = "com.auginte.desktop.HelloScalaFX"
  
  // Settings

  lazy val buildSettings = Defaults.defaultSettings ++ Seq (
    organization := buildOrganization,
    version      := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
    mainClass in(Compile, run) := Some(buildMainClass)
  )
    
  val scalaDocSettings = Seq(
    scalacOptions in(Compile, doc) ++= Opts.doc.title("Scalidea documentation"),
    scalacOptions in(Compile, doc) += "-diagrams",
    scalacOptions in(Compile, doc) += "-implicits",
    scalacOptions in(Compile, doc) += "-groups"
  )

  // Project
  lazy val allSettings = Project.defaultSettings ++ assemblySettings ++ buildSettings ++ scalaDocSettings
  lazy val withAssembly = allSettings ++ assemblySettings

  lazy val root = Project(
    id = "auginte",
    base = file(".")
  ) aggregate(
    auginteDesktop,
    auginteZooming,
    auginteTransformation,
    auginteDistribution,
    augitenteTest
    )

  lazy val auginteZooming = Project(id = "auginte-zooming",
    base = file("auginte-zooming"),
    settings = allSettings
  ) dependsOn (augitenteTest % "test->test")

  lazy val auginteTransformation = Project(id = "auginte-transformation",
    settings = allSettings,
    base = file("auginte-transformation"))

  lazy val auginteDistribution = Project(id = "auginte-distribution",
    settings = allSettings,
    base = file("auginte-distribution"))

  lazy val auginteDesktop = Project(id = "auginte-desktop",
    settings = withAssembly,
    base = file("auginte-desktop")

  ) dependsOn (auginteZooming) dependsOn(augitenteTest % "test->test")

  lazy val augitenteTest = Project(id = "auginte-test",
    settings = allSettings,
    base = file("auginte-test"))
}

