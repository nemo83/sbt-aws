organization := "com.giogar"
name := "sbt-plugin"
version := "0.0.1-SNAPSHOT"

sbtPlugin := true

val awsSdkVersion = "1.11.20"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % awsSdkVersion withSources(),
  "com.amazonaws" % "aws-java-sdk-cloudformation" % awsSdkVersion withSources(),
  "com.amazonaws" % "aws-java-sdk-codedeploy" % awsSdkVersion withSources()
)
