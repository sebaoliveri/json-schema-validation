name := "json-schema-validation"

version := "0.1"

scalaVersion := "2.13.3"

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

resolvers += Resolver.bintrayRepo("fluent-assertions", "releases")

libraryDependencies += "nulluncertainty" %% "fluent-assertions" % "2.0.1"
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.9.1"
libraryDependencies += "org.scalactic" %% "scalactic" % "3.0.8" % "test"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.8" % "test"
