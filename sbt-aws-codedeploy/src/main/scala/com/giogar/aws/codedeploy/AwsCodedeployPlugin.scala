package com.giogar.aws.codedeploy

import java.io.File

import com.amazonaws.services.codedeploy.AmazonCodeDeployClient
import com.amazonaws.services.codedeploy.model._
import com.amazonaws.services.s3.AmazonS3Client
import com.giogar.aws.AwsCommonsPlugins.autoImport._
import com.giogar.aws.builder.AwsClientBuilder
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers

object AwsCodedeployPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  private val awsClientBuilder = new AwsClientBuilder

  object autoImport {
    val push = taskKey[Unit]("Create deployment")
    val pushAndDeploy = inputKey[Unit]("Create and execute deployment")
    val deploy = inputKey[String]("Performs an AWS deployment") // All these above

    // the three steps below
    val createDeploymentArchive = taskKey[File]("AWS Cloudformation create deployment archive")
    val copyDeploymentArchiveToS3Bucket = taskKey[Unit]("Copy deployment archive to S3")
    val registerDeployment = taskKey[Unit]("AWS Cloudformation copy deployment archive")
    val createDeployment = inputKey[Unit]("Request deployment of a revision to a specific deployment group")


    // Settings
    val codedeployFolder = settingKey[String]("AWS Codedeploy Configuration folder")
    val codedeployS3Bucket = settingKey[String]("AWS Codedeploy bucket where saving the deployment artifact")
    val codedeployS3Key = settingKey[String]("Name of the artifact in AWS bucket")
    val codedeployApplicationName = settingKey[String]("Application name")

  }

  import autoImport._

  def pushTask = Def.task[Unit] {
    val deploymentFile = createDeploymentArchiveTask()
    val eTag = copyDeploymentArchiveToS3BucketTask(deploymentFile)
    registerDeploymentTask(eTag)
  }

  def createDeploymentArchiveTask(): File = {
    val source = (configurationRootFolder in aws).value.getAbsolutePath + "/" + (codedeployFolder in aws).value
    val zipFilePath = target.value + "/codedeploy/deployment.zip"
    val zipFile = new File(zipFilePath)
    IO.zip(sbt.Path.allSubpaths(new File(source)), zipFile)
    zipFile
  }

  def copyDeploymentArchiveToS3BucketTask(deploymentFile: File): String = {
    val awsRegion = (region in aws).value
    val awsCredentialsProvider = (credentialsProvider in aws).value
    val awsCodedeployBucketName = codedeployS3Bucket.value
    val key = s"${codedeployS3Key.value}-${version.value}"
    val result = awsClientBuilder
      .createAWSClient(classOf[AmazonS3Client], awsRegion, awsCredentialsProvider.toAws, null)
      .putObject(awsCodedeployBucketName, key, deploymentFile)
    result.getETag
  }

  def registerDeploymentTask(eTag: String) = {
    val awsCodedeployBucketName = codedeployS3Bucket.value
    val key = s"${codedeployS3Key.value}-${version.value}"

    val s3Location = new S3Location()
      .withBucket(awsCodedeployBucketName)
      .withKey(key)
      .withVersion(version.value)
      .withBundleType(BundleType.Zip)
      .withETag(eTag)

    val awsRegion = (region in aws).value
    val awsCredentialsProvider = (credentialsProvider in aws).value
    val applicationName = (codedeployApplicationName in aws).value

    val result = awsClientBuilder
      .createAWSClient(classOf[AmazonCodeDeployClient], awsRegion, awsCredentialsProvider.toAws, null)
      .registerApplicationRevision(
        new RegisterApplicationRevisionRequest()
          .withApplicationName(applicationName)
          .withRevision(new RevisionLocation()
            .withRevisionType(RevisionLocationType.S3)
            .withS3Location(s3Location)))
  }

  def deployTask: Def.Initialize[InputTask[String]] = Def.inputTask {
    // TODO: Implement ME!!!
    val args: Seq[String] = DefaultParsers.spaceDelimited("<arg>").parsed
    args foreach println
    //    val awsRegion = (region in aws).value
    //    val awsCodedeployConfigRoot = (awsConfigurationRootFolder in aws).value.getAbsolutePath + "/" + (codedeployFolder in aws).value
    //    println(s"CD config root: ${awsCodedeployConfigRoot}")
    //    println(s"target: ${target.value}")
    "U-123345"
  }

  def pushAndDeployTask: Def.Initialize[InputTask[Unit]] = Def.inputTask {
    pushTask.value
    deployTask.value
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    codedeployFolder in aws := "codedeploy",
    // TODO: how to deal with this below? It's mandatory, and I have no defaults!
    // codedeployS3Bucket in aws := "something",
    codedeployS3Key in aws := name.value,
    push in aws <<= pushTask,
    pushAndDeploy in aws <<= pushAndDeployTask,
    deploy in aws <<= deployTask
  )

}
