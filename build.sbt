//val organizationName =
val projectVersion = "0.0.1-SNAPSHOT"

organization in ThisBuild := "com.giogar"
name := "sbt-aws"
version := projectVersion

sbtPlugin := true

val awsSdkVersion = "1.11.20"

lazy val commonSettings = Seq(
  version := projectVersion,
  sbtPlugin := true
)

lazy val `sbt-aws-commons` = (project in file("sbt-aws-commons"))
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk" % awsSdkVersion //withSources()
    )
  )

lazy val `sbt-aws-cloudformation` = (project in file("sbt-aws-cloudformation"))
  .dependsOn(`sbt-aws-commons`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-cloudformation" % awsSdkVersion withSources()
    )
  )

lazy val `sbt-aws-codedeploy` = (project in file("sbt-aws-codedeploy"))
  .dependsOn(`sbt-aws-commons`)
  .settings(commonSettings: _*)
  .settings(
    libraryDependencies ++= Seq(
      "com.amazonaws" % "aws-java-sdk-codedeploy" % awsSdkVersion withSources(),
      "com.amazonaws" % "aws-java-sdk-s3" % awsSdkVersion withSources()
    )
  )

// TODO: delete this
libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-java-sdk" % awsSdkVersion,
  "com.amazonaws" % "aws-java-sdk-codedeploy" % awsSdkVersion,
  "com.amazonaws" % "aws-java-sdk-cloudformation" % awsSdkVersion
)