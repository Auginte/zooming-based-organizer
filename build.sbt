
ThisBuild / scalaVersion := "2.12.8"
ThisBuild / organization := "com.auginte"
ThisBuild / version := "0.9.5-SNAPSHOT"

val scalaTest = "org.scalatest" %% "scalatest" % "3.0.5"
val playJson = "com.typesafe.play" % "play-json_2.12" % "2.7.0-RC2"

lazy val auginteCommon = (project in file("auginte-common"))
  .settings(
    name := "AuginteCommon",
    libraryDependencies += scalaTest % Test
  )

lazy val auginteTransformation = (project in file("auginte-transformation"))
  .dependsOn(
    auginteCommon % "compile->compile;test->test"
  )
  .settings(
    name := "AuginteTransformation",
    libraryDependencies += scalaTest % Test
  )

lazy val auginteZooming = (project in file("auginte-zooming"))
  .dependsOn(
    auginteCommon % "compile->compile;test->test"
  )
  .settings(
    name := "AuginteZooming",
    libraryDependencies += scalaTest % Test
  )

lazy val auginteDistribution = (project in file("auginte-distribution"))
  .dependsOn(
    auginteZooming % "compile->compile;test->test",
    auginteTransformation % "compile->compile;test->test"
  )
  .settings(
    name := "AuginteDistribution",
    libraryDependencies += playJson
  )

// Determine OS version of JavaFX binaries
lazy val osName = System.getProperty("os.name") match {
  case n if n.startsWith("Linux") => "linux"
  case n if n.startsWith("Mac") => "mac"
  case n if n.startsWith("Windows") => "win"
  case _ => throw new Exception("Unknown platform!")
}
lazy val javaFXModules = Seq("base", "controls", "fxml", "graphics", "media", "swing", "web")

lazy val auginteDesktop = (project in file("auginte-desktop"))
  .dependsOn(
    auginteCommon % "compile->compile;test->test",
    auginteDistribution % "compile->compile;test->test",
    auginteTransformation % "compile->compile;test->test",
    auginteZooming % "compile->compile;test->test"
  )
  .settings(
    name := "AuginteDesktop",
    libraryDependencies += "org.scalafx" %% "scalafx" % "11-R16",
    libraryDependencies += "com.typesafe.akka" % "akka-actor_2.12" % "2.5.19",
    libraryDependencies ++= javaFXModules.map( m =>
      "org.openjfx" % s"javafx-$m" % "11" classifier osName
    ),
    packageOptions in(Compile, packageBin) += Package.ManifestAttributes("SplashScreen-Image" -> "com/auginte/common/splash.gif")
  )

lazy val auginte = (project in file("."))
  .aggregate(auginteDesktop)
  .settings(
    name := "Auginte"
  )