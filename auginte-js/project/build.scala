import sbt._
import sbt.Keys._
import sbt.TestFramework

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

object build extends sbt.Build {

  lazy val root = Project("root", file(".")).settings(
    name := "auginte-js",

    version := "0.2.0",

    scalaVersion := "2.11.6",

    persistLauncher in Compile := true,

    persistLauncher in Test := false,

    scalacOptions ++= Seq("-feature"),

    scalacOptions ++= Seq("-Xmax-classfile-name", "100"),

    testFrameworks += new TestFramework("utest.runner.Framework"),

    libraryDependencies ++= Seq(
      "org.scala-js" %%% "scalajs-dom" % "0.8.0",
      "com.lihaoyi" %%% "utest" % "0.3.0" % "test"
    ),

    // Minimal usage

    // React itself
    //   (react-with-addons.js can be react.js, react.min.js, react-with-addons.min.js)
    jsDependencies += "org.webjars" % "react" % "0.12.1" / "react-with-addons.js" commonJSName "React",

//    // Test support including ReactTestUtils
//    //   (requires react-with-addons.js instead of just react.js)
//    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "test" % "0.8.3" % "test",
//
//    // Scalaz support
//    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-scalaz71" % "0.8.3",
//
//    // Monocle support
//    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "ext-monocle" % "0.8.3",

    // Extra features
    libraryDependencies += "com.github.japgolly.scalajs-react" %%% "extra" % "0.8.3",

    libraryDependencies += "com.github.benhutchison" %%% "prickle" % "1.1.4",

    persistLauncher := true,
    mainClass := Some("example.DragableElements")
  )

  // Use local version of plugin (for transition between bug fixes)
//    .dependsOn (scalaJsReact.core)
//    .dependsOn (scalaJsReact.test % "test")
//    .dependsOn (scalaJsReact.scalaZ)
//    .dependsOn (scalaJsReact.monocle)
//    .dependsOn (scalaJsReact.extra)
//
//  object scalaJsReact {
//    val core = ProjectRef(file("/opt/gitlocal/scalajs-react"), "core")
//    val test = ProjectRef(file("/opt/gitlocal/scalajs-react"), "test")
//    val scalaZ = ProjectRef(file("/opt/gitlocal/scalajs-react"), "scalaz71")
//    val monocle = ProjectRef(file("/opt/gitlocal/scalajs-react"), "monocle")
//    val extra = ProjectRef(file("/opt/gitlocal/scalajs-react"), "extra")
//  }
}