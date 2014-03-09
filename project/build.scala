import sbt._
import Keys._

object AuginteBuild extends sbt.Build {
  lazy val root = Project(id = "auginte",
    base = file(".")) aggregate(
    zooming,
    transformation,
    distribution,
    desktop,
    test
    ) dependsOn (desktop)

  lazy val zooming = Project(id = "auginte-zooming",
    base = file("zooming")) dependsOn (test % "test->test")

  lazy val transformation = Project(id = "auginte-transformation",
    base = file("transformation"))

  lazy val distribution = Project(id = "auginte-distribution",
    base = file("distribution"))

  lazy val desktop = Project(id = "auginte-desktop",
    base = file("desktop"))

  lazy val test = Project(id = "auginte-test",
    base = file("test"))
}

