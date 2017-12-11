crossScalaVersions := Seq("2.12.4", "2.11.12")

scalaVersion := crossScalaVersions.value.head

lazy val `test-service` = project in file(".") settings (
    name := "test-service",
    version := "0.1-SNAPSHOT",
    organization := "com.test",
    resolvers ++= Seq(
      Resolver.sonatypeRepo("public")
    ),
    libraryDependencies ++= Seq(
      "io.monix" %% "monix" % "2.3.2",
      "com.typesafe.akka" %% "akka-actor" % "2.4.20",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
      "ch.qos.logback"              % "logback-classic"               % "1.2.3"
    )
)
