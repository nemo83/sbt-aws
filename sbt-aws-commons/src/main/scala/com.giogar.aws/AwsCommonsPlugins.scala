package com.giogar.aws

import java.io.File

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.Regions
import com.giogar.aws.credentials.{AwsCredentialsProvider, ProfileCredentialsProvider}
import sbt._

object AwsCommonsPlugins extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val aws = taskKey[Unit]("aws")

    val region = settingKey[Regions]("aws-region")

    // AWS Common Configuration
    val configurationRootFolder = settingKey[File]("AWS Configuration root folder")

    val credentialsProvider = settingKey[AwsCredentialsProvider]("Aws Credentials Provider to use")

  }

  import autoImport._

  // Helper Taska to avoid repetitions
  def awsRegion() = Def.task[Regions] {
    (region in aws).value
  }

  def awsConfigurationRootFolderPath() = Def.task[String] {
    (configurationRootFolder in aws).value.getAbsolutePath
  }

  def awsCredentialsProvider() = Def.task[AWSCredentialsProvider] {
    (credentialsProvider in aws).value.toAws
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    region in aws := Regions.US_EAST_1,
    configurationRootFolder in aws := file("src/main/resources/aws"),
    credentialsProvider in aws := ProfileCredentialsProvider()
  )
}
