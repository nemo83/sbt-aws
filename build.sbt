organization := "com.giogar"
name := "sbt-plugin"
version := "0.0.1-SNAPSHOT"

sbtPlugin := true

region := "dafuq"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.11.20"
)
