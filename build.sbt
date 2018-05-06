name := "graalvm-sangria-test"
version := "0.1"

scalaVersion := "2.12.6"

scalacOptions ++= Seq("-deprecation", "-feature")

mainClass in Compile := Some("Main")

retrieveManaged := true

libraryDependencies ++= Seq(
  "org.sangria-graphql" %% "sangria" % "1.4.0",
  "org.sangria-graphql" %% "sangria-circe" % "1.2.1")