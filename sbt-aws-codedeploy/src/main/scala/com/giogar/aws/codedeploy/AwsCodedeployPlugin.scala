package com.giogar.aws.codedeploy

import java.io.File

import com.amazonaws.regions.Regions
import com.amazonaws.services.codedeploy.AmazonCodeDeployClient
import com.amazonaws.services.codedeploy.model._
import com.amazonaws.services.s3.AmazonS3Client
import com.giogar.aws.AwsCommonsPlugins.autoImport._
import com.giogar.aws.builder.AwsClientBuilder
import com.giogar.aws.credentials.AwsCredentialsProvider
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
    // Zipping deployment files
    val source = (configurationRootFolder in aws).value.getAbsolutePath + "/" + (codedeployFolder in aws).value
    val zipFilePath = target.value + "/codedeploy/deployment.zip"

    val deploymentFile = createDeploymentArchiveTask(source, zipFilePath)

    // S3 copy
    val awsRegion = (region in aws).value
    val awsCredentialsProvider = (credentialsProvider in aws).value
    val awsCodedeployBucketName = (codedeployS3Bucket in aws).value
    println(s"awsCodedeployBucketName: $awsCodedeployBucketName")

    val key = s"${(codedeployS3Key in aws).value}-${version.value}"
    println(s"key: $key")

    val eTag = copyDeploymentArchiveToS3BucketTask(awsRegion,
      awsCredentialsProvider,
      awsCodedeployBucketName,
      key,
      deploymentFile)

    val applicationName = (codedeployApplicationName in aws).value
    val applicationVersion = version.value
    registerDeploymentTask(awsRegion,
      awsCredentialsProvider,
      applicationName,
      awsCodedeployBucketName,
      key,
      applicationVersion,
      eTag)

  }

  def createDeploymentArchiveTask(source: String, zipFilePath: String): File = {
    val zipFile = new File(zipFilePath)
    IO.zip(sbt.Path.allSubpaths(new File(source)), zipFile)
    zipFile
  }

  def copyDeploymentArchiveToS3BucketTask(awsRegion: Regions,
                                          awsCredentialsProvider: AwsCredentialsProvider,
                                          awsCodedeployBucketName: String,
                                          key: String,
                                          deploymentFile: File): String = {

    val result = awsClientBuilder
      .createAWSClient(classOf[AmazonS3Client], awsRegion, awsCredentialsProvider.toAws, null)
      .putObject(awsCodedeployBucketName, key, deploymentFile)
    result.getETag
  }

  def registerDeploymentTask(awsRegion: Regions,
                             awsCredentialsProvider: AwsCredentialsProvider,
                             applicationName: String,
                             awsCodedeployBucketName: String,
                             key: String,
                             applicationVersion: String,
                             eTag: String) = {

    val s3Location = new S3Location()
      .withBucket(awsCodedeployBucketName)
      .withKey(key)
      .withVersion(applicationVersion)
      .withBundleType(BundleType.Zip)
      .withETag(eTag)

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
    codedeployApplicationName in aws := name.value,
    codedeployS3Bucket in aws := name.value,
    codedeployS3Key in aws := name.value,
    push in aws <<= pushTask,
    pushAndDeploy in aws <<= pushAndDeployTask,
    deploy in aws <<= deployTask
  )

}
