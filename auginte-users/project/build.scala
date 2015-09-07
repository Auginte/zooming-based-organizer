import sbt.Keys._
import sbt._

object build extends sbt.Build {
  val buildName = "auginte-users"
  val buildVersion = "0.3.1"
  val buildScalaVersion = "2.11.7"
  val buildOptions = Seq("-feature", "-unchecked", "-deprecation", "-encoding", "utf8")

  val akkaVersion = "2.3.12"
  val sprayVersion = "1.3.3"
  val buildDependencies = Seq(
    "io.spray" %% "spray-can" % sprayVersion,
    "com.typesafe.akka" % "akka-actor_2.11" % akkaVersion,
    "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
  )

  lazy val commonSettings = Seq(
    name := buildName,
    version := buildVersion,
    scalaVersion := buildScalaVersion,
    scalacOptions := buildOptions,
    mainClass := Some("com.auginte.users.Main"),
    libraryDependencies ++= buildDependencies,
    scalacOptions in(Compile, doc) ++= Seq("-diagrams"),
    spray.revolver.RevolverPlugin.Revolver.settings
  )

  lazy val scarangoMacros = RootProject(file("../../../scarango/scarango"))

  lazy val auginteUsers = (project in file(".")
    settings (commonSettings: _*)
    settings (less.Plugin.lessSettings:_*)
    // dependsOn scarangoMacros
    )
}
