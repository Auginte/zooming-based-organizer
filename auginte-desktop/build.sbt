// Use from main project
// mainClass in(Compile, run) := Some("com.auginte.desktop.MainGui")

// Replaced by ScalaFx
//libraryDependencies += "org.scala-lang" % "scala-swing" % "2.10.0"

// Add dependency on ScalaFX library, for use with JavaFX 2.2/Java 7
libraryDependencies += "org.scalafx" %% "scalafx" % "1.0.0-R8"

// Add dependency on ScalaFX library, for use with JavaFX 8/Java 8
// libraryDependencies += "org.scalafx" %% "scalafx" % "8.0.0-M3"

// Add dependency on JavaFX library based on JAVA_HOME variable
unmanagedJars in Compile += Attributed.blank(file(System.getenv("JAVA_HOME") + "/jre/lib/jfxrt.jar"))
// Save java JAVA_HOME in ~/.bashrc. E.g. export JAVA_HOME=/usr/lib/jvm/java-7-oracle

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.5"

// Fork a new JVM for 'run' and 'test:run', to avoid JavaFX double initialization problems
fork := true
