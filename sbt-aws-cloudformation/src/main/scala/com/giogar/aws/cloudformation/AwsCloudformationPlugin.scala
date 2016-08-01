import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.{CreateStackRequest, DeleteStackRequest, UpdateStackRequest}
import com.giogar.aws.AwsCommonsPlugins.autoImport._
import com.giogar.aws.AwsCommonsPlugins
import com.giogar.aws.builder.AwsClientBuilder
import sbt.Keys._
import sbt._

import scala.collection.JavaConverters._

object AwsCloudformationPlugin extends AutoPlugin {

  override def requires: Plugins = AwsCommonsPlugins

  override def trigger: PluginTrigger = allRequirements

  private val awsClientBuilder = new AwsClientBuilder

  object autoImport {

    // ** AWS Cloudformation **
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

  def createStackTask() = Def.task {
    val awsRegion = (region in aws).value
    val awsProfile = (profile in aws).value
    val awsCloudformationConfigRoot = (awsConfigurationRootFolder in aws).value.getAbsolutePath + "/" + (cloudformationFolder in aws).value
    val stackName = (cloudformationStackName in aws).value
    val cloudformationTemplatePath = awsCloudformationConfigRoot + "/" + (cloudformationTemplateFilename in aws).value

    val capability = (capabilitiesIam in aws).value

    println(s"CFN config root: ${awsCloudformationConfigRoot}")
    println(s"CFN file: ${cloudformationTemplatePath}")

    val stackRequest = new CreateStackRequest()
    stackRequest.setStackName(stackName)
    stackRequest.setCapabilities(Set(capability).asJava)
    stackRequest.setTemplateBody(scala.io.Source.fromFile(cloudformationTemplatePath).mkString)

    val stackId = awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion, new ProfileCredentialsProvider(s"${sys.env("HOME")}/.aws/credentials", awsProfile), null)
      .createStack(stackRequest)
      .getStackId

    println(s"stackId: $stackId")

  }

  def updateStackTask() = Def.task {
    val awsRegion = (region in aws).value
    val awsProfile = (profile in aws).value
    val awsCloudformationConfigRoot = (awsConfigurationRootFolder in aws).value.getAbsolutePath + "/" + (cloudformationFolder in aws).value
    val stackName = (cloudformationStackName in aws).value
    val cloudformationTemplatePath = awsCloudformationConfigRoot + "/" + (cloudformationTemplateFilename in aws).value

    val capability = (capabilitiesIam in aws).value

    println(s"CFN config root: ${awsCloudformationConfigRoot}")
    println(s"CFN file: ${cloudformationTemplatePath}")

    val stackRequest = new UpdateStackRequest()
    stackRequest.setStackName(stackName)
    stackRequest.setCapabilities(Set(capability).asJava)
    stackRequest.setTemplateBody(scala.io.Source.fromFile(cloudformationTemplatePath).mkString)

    val stackId = awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion, new ProfileCredentialsProvider(s"${sys.env("HOME")}/.aws/credentials", awsProfile), null)
      .updateStack(stackRequest)
      .getStackId

    println(s"stackId: $stackId")
  }

  def deleteStackTask(): Def.Initialize[Task[Unit]] = Def.task {
    val awsRegion = (region in aws).value
    val awsProfile = (profile in aws).value
    val awsCloudformationConfigRoot = (awsConfigurationRootFolder in aws).value.getAbsolutePath + "/" + (cloudformationFolder in aws).value
    val stackName = (cloudformationStackName in aws).value
    val cloudformationTemplatePath = awsCloudformationConfigRoot + "/" + (cloudformationTemplateFilename in aws).value

    println(s"CFN config root: ${awsCloudformationConfigRoot}")
    println(s"CFN file: ${cloudformationTemplatePath}")

    val stackRequest = new DeleteStackRequest()
    stackRequest.setStackName(stackName)

    awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion, new ProfileCredentialsProvider(s"${sys.env("HOME")}/.aws/credentials", awsProfile), null)
      .deleteStack(stackRequest)

  }

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    // AWS Cloudformation
    cloudformationStackName in aws := name.value,
    cloudformationFolder in aws := "cloudformation",
    cloudformationTemplateFilename in aws := "cloudformation.template",
    createStack in aws <<= createStackTask(),
    updateStack in aws <<= updateStackTask(),
    deleteStack in aws <<= deleteStackTask()
  )

}
