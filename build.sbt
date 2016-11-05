name := """importnow"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.eclipse.rdf4j" % "rdf4j-runtime" % "2.1",
  "com.github.tototoshi" %% "scala-csv" % "1.3.3",
//  "com.opencsv" % "opencsv" % "3.8",
//  "net.sourceforge.javacsv" % "javacsv" % "2.0",
  jdbc,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

// no fork, to make it easier to kill together with cli command
fork in run := false

fork in run := true