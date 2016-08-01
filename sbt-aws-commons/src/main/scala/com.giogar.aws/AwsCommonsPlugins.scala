package com.giogar.aws

import java.io.File

import com.amazonaws.regions.Regions
import sbt._

object AwsCommonsPlugins extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val aws = taskKey[Unit]("aws")

    val region = settingKey[Regions]("aws-region")

    val profile = settingKey[String]("aws-profile")

    // AWS Common Configuration
    val awsConfigurationRootFolder = settingKey[File]("AWS Configuration root folder")

  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    // AWS Common
    region in aws := Regions.US_EAST_1,
    awsConfigurationRootFolder in aws := file("src/main/resources/aws"),
    profile in aws := "default"
  )
}
