import java.io._
import java.text.SimpleDateFormat
import java.util.{Date, Properties}
import java.util.zip.{ZipEntry, ZipOutputStream}

import sbt.Keys._
import sbt.Process
import sbt._
import sbtassembly.Plugin._
import xerial.sbt.Pack._
import java.{io => jio}
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import playscalajs.ScalaJSPlay
import playscalajs.ScalaJSPlay.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import playscalajs.PlayScalaJS.autoImport._
import com.typesafe.sbt.web.Import._
import com.typesafe.sbt.gzip.Import._
import play.PlayScala
import sbt.Project.projectToRef
import com.typesafe.sbt.web.SbtWeb
import com.typesafe.sbt.less.Import._

object build extends sbt.Build {
  val buildName = "auginte"
  val buildOrganization = "com.autinte"
  val buildVersion      = ProjectProperties.getProperty("version", default="0.0.1-SNAPSHOT")
  val buildScalaVersion = "2.11.6"
  val buildMainClass = "com.auginte.desktop.Auginte"

  // Custom properties (also accessable from source)

  // Settings


  lazy val buildSettings = Seq (
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

  val fixForEncryptedFileSystems = Seq(
    scalacOptions ++= Seq("-Xmax-classfile-name", "100")
  )


  val packCustomSettings = Seq(
	  packExtraClasspath := Map("auginte" -> Seq("${JAVA_HOME}/jre/lib/jfxrt.jar", "%JAVA_HOME%\\lib\\jfxrt.jar")),
    packBashTemplate := "./project/templates/launch.mustache",
    packResourceDir += (baseDirectory.value / "auginte-desktop/src/pack" -> "")
  )

  val customTasks = Seq(CustomTasks.deployTask)

  val scalaJsSettings = Seq(

  )

  // Project
  lazy val allSettings = buildSettings ++ scalaDocSettings ++ packAutoSettings ++ packCustomSettings ++ customTasks ++
    fixForEncryptedFileSystems
  lazy val withAssembly = allSettings ++ assemblySettings

  lazy val auginte = (project in file(".")).
    settings(
      name := "auginte",
      version := buildVersion,
      scalaVersion := buildScalaVersion
    ).
    aggregate(
      auginteDesktop,
      auginteZooming,
      auginteTransformation,
      auginteDistribution,
      auginteCommon
    )


  lazy val auginteZooming = (project in file("auginte-zooming"))
    .settings(
      name := "auginte-zooming",
      version := buildVersion,
      scalaVersion := buildScalaVersion
    ).
    dependsOn(auginteCommon).
    dependsOn(auginteCommon % "test->test")

  lazy val auginteTransformation = (project in file("auginte-transformation")).
    settings(
      name := "auginte-transformation",
      version := buildVersion,
      scalaVersion := buildScalaVersion
    ).
    dependsOn(auginteCommon).
    dependsOn(auginteCommon % "test->test")

  lazy val auginteDistribution = (project in file("auginte-distribution")).
    settings(
    name := "auginte-distribution",
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    libraryDependencies ++= Seq(
      "com.typesafe.play" %% "play-json" % "2.3.9",
      "com.orientechnologies" % "orientdb-core" % orientDbVersionServer,
      "com.orientechnologies" % "orientdb-graphdb" % orientDbVersionServer
    )
  ).
    dependsOn(auginteZooming).
    dependsOn(auginteTransformation).
    dependsOn(auginteZooming % "test->test").
    dependsOn(auginteCommon % "test->test")

  lazy val auginteDesktop = (project in file("auginte-desktop")).
    settings(
      name := "auginte-desktop",
      version := buildVersion,
      scalaVersion := buildScalaVersion
    ).
    dependsOn(auginteDistribution).
    dependsOn(auginteDistribution % "test->test").
    dependsOn(auginteCommon % "test->test")

  lazy val auginteCommon = (project in file("auginte-common"))
    .settings(
      name := "auginte-common",
      version := buildVersion,
      scalaVersion := buildScalaVersion
    )

  lazy val clients = Seq(auginteJs)

  val orientDbVersionServer = "2.0.9"

  lazy val auginteServer = (project in file("auginte-server")).settings(
    name := "auginte-server",
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalaJSProjects := clients,
    pipelineStages := Seq(scalaJSProd, gzip),
    libraryDependencies ++= Seq(
      "com.vmunier" %% "play-scalajs-scripts" % "0.2.1",
      "org.webjars.bower" % "react" % "0.13.3",
      "com.orientechnologies" % "orientdb-core" % orientDbVersionServer,
      "com.orientechnologies" % "orientdb-graphdb" % orientDbVersionServer,
      "com.github.benhutchison" %% "prickle" % "1.1.5",
      "org.scalatest" % "scalatest_2.11" % "2.2.1" % "test"
    ),
    includeFilter in (Assets, LessKeys.less) := "*.less"
  ).enablePlugins(PlayScala).
    enablePlugins(SbtWeb).
    aggregate(clients.map(projectToRef): _*).
    dependsOn(auginteShared.jvm)

  lazy val auginteJs = (project in file("auginte-js")).settings(
    name := "auginte-js",
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    persistLauncher := true,
    persistLauncher in Test := false,
    sourceMapsDirectories += auginteSharedJs.base / "..",
    unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "com.lihaoyi" %%% "utest" % "0.3.0" % "test",
      "com.github.japgolly.scalajs-react" %%% "extra" % "0.8.3",
      "com.github.benhutchison" %%% "prickle" % "1.1.5"
    ),
    jsDependencies += "org.webjars" % "react" % "0.12.1" / "react-with-addons.js" commonJSName "React",
    persistLauncher := true,
    mainClass := Some("example.DragableElements")
  ).enablePlugins(ScalaJSPlugin, ScalaJSPlay).
    dependsOn(auginteShared.js)

  lazy val auginteShared = (crossProject.crossType(CrossType.Pure) in file("auginte-shared")).
    settings(
      name := "auginte-shared",
      version := buildVersion,
      scalaVersion := buildScalaVersion
    ).
    jsConfigure(_ enablePlugins ScalaJSPlay).
    jsSettings(sourceMapsBase := baseDirectory.value / "..")

  lazy val auginteSharedJvm = auginteShared.jvm
  lazy val auginteSharedJs = auginteShared.js

  // loads the Play project at sbt startup
  onLoad in Global := (Command.process("project auginteServer", _: State)) compose (onLoad in Global).value
}

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

object CustomTasks {
  private val proguardPath = "/usr/bin/proguard"
  private val proguardTemplate = "project/proguard/auginte-template.pro"
  private val java7Dependencies = List(
    "<java.home>/lib/jfxrt.jar",
    "<java.home>/lib/rt.jar"
  )

  private val deploy = TaskKey[Unit]("deploy", "Prints 'Hello World'")

  val deployTask = deploy := {
    val log = streams.value.log
    val packedTo = pack.value.getAbsolutePath
    log.info(s"Packed to: $packedTo")
    val runFile = findRunFiles(packedTo)
    if (runFile.isDefined) {
      val revision = saveGitVersion(s"$packedTo/VERSION")
      log.info(s"GIT revision: $revision")
      log.info("Running Proguard...")
      val executed = Proguard.obfuscateAuginte(
        ProguardTemplate(proguardTemplate, java7Dependencies, packedTo + "/lib"),
        proguardPath
      )
      log.info(s"Obfuscated: $executed")
      log.info(s"Can be executed via: " + runFile.head)
      log.info("Creating archive...")
      val zipped = createArchive(packedTo)
      log.info(s"Archive placed in: " + zipped)
    }
  }

  private def saveGitVersion(path: String): String = {
    val revision: String = "git rev-parse HEAD".!!
    val date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(new Date())
    val out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)))
    out.println(s"gitRevision:=${revision.trim}")
    out.println(s"buildDate:=$date")
    out.close()
    revision
  }

  private def createArchive(path: String, project: String = "auginte"): String = {
    def writeData(file: File, out: ZipOutputStream): Unit = {
      val buffer = new Array[Byte](1024)
      var bytesRead: Int = 0
      val input = new FileInputStream(file)
      do {
        bytesRead = input.read(buffer)
        if (bytesRead > 0) {
          out.write(buffer, 0, bytesRead)
        }
      } while (bytesRead > 0)
      input.close()
    }

    val version = ProjectProperties.getProperty("version", default = "")
    val destination = new jio.File(s"$path/../$project-$version.zip").getAbsoluteFile
    if (destination.exists()) {
      destination.delete()
    }

    val archive = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(destination)))
    archive.setLevel(9)
    for (file <- getRecursiveListOfFiles(new File(path)) if file.isFile) {
      val pathInArchive = file.toString.substring(path.length)
      archive.putNextEntry(new ZipEntry(pathInArchive))
      writeData(file, archive)
      archive.closeEntry()
    }
    archive.close()
    destination.toString
  }

  private def getRecursiveListOfFiles(dir: File): Array[File] = {
    val these = dir.listFiles
    these ++ these.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
  }

  private def findRunFiles(path: String) = new File(path + "/bin").listFiles().find(!_.getName.endsWith(".bat"))
}

case class ProguardTemplate(path: String, additionalLibraries: Seq[String], libraryPath: String) {
  protected val projectPrefix = "auginte-"

  private val libraries = new jio.File(libraryPath).listFiles

  def auginteLibraries: Seq[String] = libraries filter (_.getName.startsWith(projectPrefix)) map (_.toString)

  def otherLibraries: Seq[String] = libraries map (_.toString) diff auginteLibraries
}

object Proguard {
  private def prepareConfiguration(template: ProguardTemplate): String = {
    val configuration = File.createTempFile("proguardConfiguration-", ".pro")

    val writer = new PrintWriter(new BufferedWriter(new FileWriter(configuration)))
    for (inJar <- template.auginteLibraries) {
      val manifest = if (inJar.contains("-desktop_")) "" else "(!META-INF/MANIFEST.MF)"
      writer.println(s"-injars $inJar$manifest")
    }
    writer.println(s"-outjars ${template.libraryPath}/auginte.jar")
    for (libraryJar <- template.otherLibraries ++ template.additionalLibraries) {
      writer.println(s"-libraryjars $libraryJar")
    }
    writer.println()
    for (line <- scala.io.Source.fromFile(template.path).getLines()) {
      writer.println(line)
    }
    writer.close()

    configuration.toString
  }

  def obfuscateAuginte(template: ProguardTemplate, proguard: String): String = {
    val configuration = prepareConfiguration(template)
    val command = s"$proguard @$configuration"
    command.!
    for (duplicate <- template.auginteLibraries) {
      new File(duplicate).delete()
    }
    command
  }
}