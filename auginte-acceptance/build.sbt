import sbt.Keys._

name := "auginte-acceptance"

organization := "com.auginte"

version := "0.8.1"

scalaVersion := "2.11.6"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"

libraryDependencies += "pl.oakfusion" % "chromedriver" % "2.9" % "test"

libraryDependencies += "com.github.detro.ghostdriver" % "phantomjsdriver" % "1.1.0" % "test"




