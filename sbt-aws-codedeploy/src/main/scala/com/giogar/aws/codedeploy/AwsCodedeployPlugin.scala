package com.giogar.aws.codedeploy
import com.amazonaws.regions.Regions
import com.giogar.aws.AwsCommonsPlugins.autoImport._
import com.giogar.aws.builder.AwsClientBuilder
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers

object AwsCodedeployPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  private val awsClientBuilder = new AwsClientBuilder

  object autoImport {

    // ** AWS Codedeploy **
    val codedeployPush = taskKey[Unit]("Create deployment archive and push it to S3")
    // the three steps below
    val createDeploymentArchive = taskKey[File]("AWS Cloudformation create deployment archive")
    val copyDeploymentArchiveToS3Bucket = taskKey[Unit]("Copy deployment archive to S3")
    val registerDeployment = taskKey[Unit]("AWS Cloudformation copy deployment archive")
    val createDeployment = inputKey[Unit]("Request deployment of a revision to a specific deployment group")

    val deploy = inputKey[String]("Performs an AWS deployment") // All these above

    // Settings
    val codedeployFolder = settingKey[String]("AWS Codedeploy Configuration folder")

  }

  import autoImport._

  def createDeploymentArchiveTask(): Def.Initialize[Task[sbt.File]] = Def.task {
    val source = (awsConfigurationRootFolder in aws).value.getAbsolutePath + "/" + (codedeployFolder in aws).value
    val zipFilePath = target.value + "/codedeploy/deployment.zip"
    val zipFile = new File(zipFilePath)
    IO.zip(sbt.Path.allSubpaths(new File(source)), zipFile)
    zipFile
  }

  def deployTask(): Def.Initialize[InputTask[String]] = Def.inputTask {
    val args: Seq[String] = DefaultParsers.spaceDelimited("<arg>").parsed
    args foreach println
    //    val awsRegion = (region in aws).value
    //    val awsCodedeployConfigRoot = (awsConfigurationRootFolder in aws).value.getAbsolutePath + "/" + (codedeployFolder in aws).value
    //    println(s"CD config root: ${awsCodedeployConfigRoot}")
    //    println(s"target: ${target.value}")
    "U-123345"
  }


  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    region in aws := Regions.US_EAST_1,
    // AWS Common
    awsConfigurationRootFolder in aws := file("src/main/resources/aws"),
    // AWS Codedeploy
    codedeployFolder in aws := "codedeploy",
    createDeploymentArchive in aws <<= createDeploymentArchiveTask(),
    deploy in aws <<= deployTask()
  )

}
