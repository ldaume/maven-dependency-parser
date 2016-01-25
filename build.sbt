name := "dependency-parser"

organization := "software.reinvent"

version := "1.0.0"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.2.1",
  "org.scala-lang" % "scala-library" % "2.11.7",

  // Commons
  "org.apache.commons" % "commons-lang3" % "3.4",
  "com.google.guava" % "guava" % "19.0-rc2",
  "org.apache.commons" % "commons-collections4" % "4.0",
  "commons-io" % "commons-io" % "2.4",
  "com.beust" % "jcommander" % "1.48",
  "ch.qos.logback" % "logback-classic" % "1.1.3",
  "ch.qos.logback" % "logback-core" % "1.1.3",

  // HTTP
  "com.typesafe.play" % "play-java-ws_2.11" % "2.4.6",

  // CSV
  "com.opencsv" % "opencsv" % "3.6",

  // Maven
  "org.apache.maven" % "maven-model" % "3.3.9",

  // Testing
  "org.assertj" % "assertj-core" % "3.1.0" % "test",
  "junit" % "junit" % "4.12" % "test",
  "com.novocode" % "junit-interface" % "0.10" % "test"
)

mainClass in Compile := Some("software.reinvent.dependency.parser.application.DependencyParser")

// This becomes the Start Menu subdirectory for the windows installers.
maintainer := "RE:invent Software, Leonard Daume"

packageSummary := "Maven Dependency Parser"

packageDescription := "Generates CSV files of internal and external artifacts and their dependencies by parsing all pom files."

enablePlugins(JavaAppPackaging,JDKPackagerPlugin)

//javacOptions ++= Seq("-source", "1.8", "-target", "1.8")

scalaVersion := "2.11.7"

resolvers += Resolver.mavenLocal

//publishMavenStyle := true

//crossPaths := false

//autoScalaLibrary := false


jdkPackagerType := "all"

jdkPackagerToolkit := JavaFXToolkit

jdkPackagerJVMArgs := Seq("-Xmx1g", "-Xdiag")

jdkPackagerJVMArgs := Seq("-Xmx1g")

jdkPackagerProperties := Map("app.name" -> name.value, "app.version" -> version.value)



