package com.giogar.aws.cloudformation

import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.{CreateStackRequest, DeleteStackRequest, DescribeStackResourceRequest, UpdateStackRequest}
import com.giogar.aws.AwsCommonsPlugins
import com.giogar.aws.AwsCommonsPlugins._
import com.giogar.aws.AwsCommonsPlugins.autoImport._
import com.giogar.aws.builder.AwsClientBuilder
import sbt.Keys._
import sbt._

import scala.collection.JavaConverters._

object AwsCloudformationPlugin extends AutoPlugin {

  override def requires: Plugins = AwsCommonsPlugins

  override def trigger: PluginTrigger = allRequirements

  private val awsClientBuilder = new AwsClientBuilder

  object autoImport {
    // Tasks
    val createStack = taskKey[Unit]("create-stack")
    val updateStack = taskKey[Unit]("update-stack")
    val deleteStack = taskKey[Unit]("delete-stack")

    // Settings
    val capabilitiesIam = settingKey[String]("AWS capabilities IAM")
    val cloudformationStackName = settingKey[String]("AWS Cloudformation Stack name")
    val cloudformationFolder = settingKey[String]("AWS Cloudformation folder")
    val cloudformationTemplateFilename = settingKey[String]("AWS Cloudformation filename")
  }

  import autoImport._


  // Helper tasks to avoid repetition
  def awsCloudformationConfigRoot() = Def.task[String] {
    awsConfigurationRootFolderPath.value + "/" + (cloudformationFolder in aws).value
  }

  def stackName() = Def.task[String] {
    (cloudformationStackName in aws).value
  }

  def cloudformationTemplateBody() = Def.task[String] {
    val cloudformationTemplatePath = awsCloudformationConfigRoot.value + "/" + (cloudformationTemplateFilename in aws).value
    scala.io.Source.fromFile(cloudformationTemplatePath).mkString
  }

  def capability() = Def.task[String] {
    (capabilitiesIam in aws).value
  }

  def createStackTask() = Def.task {
    val stackRequest = new CreateStackRequest()
      .withStackName(stackName.value)
      .withCapabilities(Set(capability.value).asJava)
      .withTemplateBody(cloudformationTemplateBody.value)

    val stackId = awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion.value, awsCredentialsProvider.value, null)
      .createStack(stackRequest)
      .getStackId
  }

  def updateStackTask() = Def.task {
    val stackRequest = new UpdateStackRequest()
      .withStackName(stackName.value)
      .withCapabilities(Set(capability.value).asJava)
      .withTemplateBody(cloudformationTemplateBody.value)

    val stackId = awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion.value, awsCredentialsProvider.value, null)
      .updateStack(stackRequest)
      .getStackId

  }

  def deleteStackTask(): Def.Initialize[Task[Unit]] = Def.task {
    val stackRequest = new DeleteStackRequest()
      .withStackName(stackName.value)
    awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion.value, awsCredentialsProvider.value, null)
      .deleteStack(stackRequest)
  }

  def getPhysicalResourceIdTask(logicalResourceName: String) = Def.task[String] {
    val describeResourceRequest = new DescribeStackResourceRequest() // this could be DescribeStackS instead!!
      .withStackName(stackName.value)
      .withLogicalResourceId(logicalResourceName)

    awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion.value, awsCredentialsProvider.value, null)
      .describeStackResource(describeResourceRequest)
      .getStackResourceDetail
      .getPhysicalResourceId
  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    cloudformationStackName in aws := name.value,
    cloudformationFolder in aws := "cloudformation",
    cloudformationTemplateFilename in aws := "cloudformation.template",
    createStack in aws <<= createStackTask(),
    updateStack in aws <<= updateStackTask(),
    deleteStack in aws <<= deleteStackTask()
  )

}
