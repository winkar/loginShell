name := "Automator"

organization := "com.winkar"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.11.8"

libraryDependencies += "log4j" % "log4j" % "1.2.14"
libraryDependencies += "junit" % "junit" % "4.12"
libraryDependencies += "io.appium" % "java-client" % "3.4.0"
libraryDependencies += "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"
libraryDependencies += "com.github.scopt" %% "scopt" % "3.4.0"
libraryDependencies += "com.github.nscala-time" %% "nscala-time" % "2.12.0"
libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.1"

resolvers += Resolver.sonatypeRepo("public")


