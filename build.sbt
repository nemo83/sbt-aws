organization := "com.giogar"
name := "sbt-plugin"
version := "0.0.1-SNAPSHOT"

sbtPlugin := true

//region in aws := com.amazonaws.regions.Regions.US_WEST_1

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % "1.11.20"
)
