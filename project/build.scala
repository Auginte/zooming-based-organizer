import java.io.FileInputStream
import java.util.Properties

import com.typesafe.sbt.less.Import._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.web.SbtWeb
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import play.sbt.PlayScala
import playscalajs.PlayScalaJS.autoImport._
import playscalajs.ScalaJSPlay
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

object build extends sbt.Build {
  // Main configuration
  val buildName = "auginte"
  val buildOrganization = "com.autinte"
  val buildVersion = ProjectProperties.getProperty("version", default = "0.0.1-SNAPSHOT")
  val buildScalaVersion = "2.11.8"
  val buildMainClass = "com.auginte.desktop.Auginte"

  // Common settings
  override lazy val settings = super.settings ++
    Seq(
      organization := buildOrganization,
      version := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature"),
      scalacOptions += "-Ylog-classpath",
      mainClass in(Compile, run) := Some(buildMainClass)
    )


  // Container for all subprojects
  lazy val root = Project(
    id = "auginte",
    base = file("."),
    settings = Seq(
      description := "Auginte - platform to augment your intelligence"
    )
  )
    .aggregate(auginteZooming)
    .aggregate(auginteTransformation)
    .aggregate(auginteDistribution)
    .aggregate(auginteDesktop)
    .aggregate(`auginte-server`)


  //
  // Sub projects / components
  //

  lazy val clients = Seq(`auginte-js`)
  lazy val `auginte-server` = (project in file("auginte-server")).settings(
    name := "auginte-server",
    description := "Web version of zooming based organiser. ScalaJS frontend, Play+OrientDB backend",
    libraryDependencies ++= Seq(
      "com.vmunier" %% "play-scalajs-scripts" % "0.4.0",
      "org.webjars.bower" % "react" % "0.13.3",
      "com.orientechnologies" % "orientdb-core" % orientDbVersionServer,
      "com.orientechnologies" % "orientdb-graphdb" % orientDbVersionServer,
      "com.github.benhutchison" %% "prickle" % "1.1.5"
    ),
    scalaJSProjects := clients,
    includeFilter in(Assets, LessKeys.less) := "*.less"
  )
    .enablePlugins(PlayScala)
    .enablePlugins(SbtWeb)
    .aggregate(clients.map(projectToRef): _*)
    .dependsOn(`auginte-shared`.jvm)
    .dependsOn(auginteCommon)
    .dependsOn(auginteCommon % "test->test")

  lazy val `auginte-js` = (project in file("auginte-js")).settings(
    name := "auginte-js",
    description := "Frontend for Web version of zooming based organiser. Using Facebook React",
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    persistLauncher := true,
    persistLauncher in Test := false,
    unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.8.3",
      "com.github.benhutchison" %%% "prickle" % "1.1.5"
    ),
    jsDependencies += "org.webjars" % "react" % "0.13.3" / "react-with-addons.js" commonJSName "React",
    persistLauncher := true,
    mainClass := Some("example.DragableElements")
  ).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
    dependsOn(`auginte-shared`.js)

  lazy val `auginte-shared` = (crossProject.crossType(CrossType.Pure) in file("auginte-shared")).
    settings(
      name := "auginte-shared",
      version := buildVersion,
      scalaVersion := buildScalaVersion
    )
    .jsConfigure(_ enablePlugins ScalaJSPlay)

  lazy val auginteSharedJvm = `auginte-shared`.jvm
  lazy val auginteSharedJs = `auginte-shared`.js

  val splashScreen = "com/auginte/common/splash.gif"
  lazy val auginteDesktop = Project(
    id = "auginte-desktop",
    base = file("auginte-desktop"),
    settings = Seq(
      description := "PC (GUI) version of zooming based organiser. Using JavaFX as GUI engine",
      fork := true,
      resolvers += Opts.resolver.sonatypeSnapshots,
      libraryDependencies ++= Seq(
        "org.scalafx" %% "scalafx" % "8.0.60-R9", // JavaFX 8/Java 8
        //"org.scalafx" %% "scalafx" % "2.2.76-R11", // JavaFX 2.2/Java 7
        "com.typesafe.akka" %% "akka-actor" % "2.3.11"
      ),
      unmanagedJars in Compile += Attributed.blank(file(javaHomePath + "/jre/lib/jfxrt.jar")),
      packageOptions in(Compile, packageBin) += Package.ManifestAttributes("SplashScreen-Image" -> splashScreen)
    )
  )
    .dependsOn(auginteDistribution)
    .dependsOn(auginteDistribution % "test->test")

  val orientDbVersionServer = "2.1.4"
  lazy val auginteDistribution = Project(
    id = "auginte-distribution",
    base = file("auginte-distribution"),
    settings = Seq(
      description := "Storage, importing and exporting of data. Implemented on top of OrientDB",
      fork := true,
      libraryDependencies ++= Seq(
        "com.typesafe.play" %% "play-json" % "2.3.9",
        "com.orientechnologies" % "orientdb-core" % orientDbVersionServer,
        ("com.orientechnologies" % "orientdb-graphdb" % orientDbVersionServer).
          exclude("org.mortbay.jetty", "servlet-api").
          exclude("commons-beanutils", "commons-beanutils-core").
          exclude("commons-collections", "commons-collections").
          exclude("commons-logging", "commons-logging").
          exclude("com.esotericsoftware.minlog", "minlog")
      )
    )
  )
    .dependsOn(auginteTransformation)
    .dependsOn(auginteZooming)
    .dependsOn(auginteZooming % "test->test")
    .dependsOn(auginteCommon % "test->test")

  lazy val auginteTransformation = Project(
    id = "auginte-transformation",
    base = file("auginte-transformation"),
    settings = Seq(
      description := "Source tracking: Store relations between original and derived elements"
    )
  )
    .dependsOn(auginteCommon)
    .dependsOn(auginteCommon % "test->test")

  lazy val auginteZooming = Project(
    id = "auginte-zooming",
    base = file("auginte-zooming"),
    settings = Seq(
      description := "Infinity zooming/grid: move and scale elements and camera"
    )
  )
    .dependsOn(auginteCommon)
    .dependsOn(auginteCommon % "test->test")

  lazy val auginteCommon = Project(
    id = "auginte-common",
    base = file("auginte-common"),
    settings = Seq(
      description := "Common code for component versioning and test infrastructure",
      libraryDependencies ++= Seq(
        "org.scalatest" %% "scalatest" % "2.2.6" % "test"
      )
    )
  )

  //
  // Utilities
  //

  def javaHomePath = {
    val environmentVariable = System.getenv("JAVA_HOME")
    if (environmentVariable != "") environmentVariable else "/usr/lib/jvm/java-8-oracle"
  }

  def debugTravis(): Unit = {
    println("Environment JAVA_HOME: " + System.getenv("JAVA_HOME"))
    println("Final javaHomePath: " + javaHomePath)
    println("JRE libraries: " + new File(javaHomePath + "/jre/lib").list().mkString("\n\t", "\n\t","\n"))
  }
  debugTravis()

  object ProjectProperties {
    lazy val customProperties: Option[Properties] = try {
      val properties = new Properties()
      properties.load(new FileInputStream("./auginte-common/src/main/resources/com/auginte/common/build.properties"))
      Some(properties)
    } catch {
      case e: Exception => None
    }

    def getProperty(name: String, default: String = ""): String = customProperties match {
      case Some(p) if p.getProperty(name) != null => p.getProperty(name)
      case _ => default
    }
  }
}