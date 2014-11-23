resolvers += "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.3"

// Orient DB
val orientDBVersion = "2.0-M1"

libraryDependencies += "com.orientechnologies" % "orient-commons" % orientDBVersion

libraryDependencies += "com.orientechnologies" % "orientdb-core" % orientDBVersion

libraryDependencies += "com.orientechnologies" % "orientdb-graphdb" % orientDBVersion

libraryDependencies += "com.tinkerpop.blueprints" % "blueprints-core" % "2.6.0"
