import java.io.FileInputStream
import java.util.Properties

import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._

object AuginteBuild extends sbt.Build {
  val buildName = "auginte"
  val buildOrganization = "com.autinte"
  val buildVersion      = getProperty("version", default="0.0.1-SNAPSHOT")
  val buildScalaVersion = "2.10.3"
  val buildMainClass = "com.auginte.desktop.HelloScalaFX"

  // Custom properties (also accessable from source)

  lazy val customProperties: Option[Properties] = try {
    val properties = new Properties()
    properties.load(new FileInputStream("./auginte-test/src/main/resources/com/auginte/build.properties"))
    Some(properties)
  } catch {
    case e: Exception => None
  }

  private def getProperty(name: String, default: String = ""): String = customProperties match {
    case Some(p) if p.getProperty(name) != null => p.getProperty(name)
    case _ => default
  }


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
    base = file("auginte-distribution")
  ) dependsOn auginteZooming dependsOn augitenteTest dependsOn(augitenteTest % "test->test")

  lazy val auginteDesktop = Project(id = "auginte-desktop",
    settings = withAssembly,
    base = file("auginte-desktop")
  ) dependsOn auginteDistribution dependsOn(augitenteTest % "test->test")

  lazy val augitenteTest = Project(id = "auginte-test",
    settings = allSettings,
    base = file("auginte-test"))
}

