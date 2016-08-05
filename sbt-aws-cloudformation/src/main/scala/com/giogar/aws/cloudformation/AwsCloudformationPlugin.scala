import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.{CreateStackRequest, DeleteStackRequest, UpdateStackRequest}
import com.giogar.aws.AwsCommonsPlugins
import com.giogar.aws.AwsCommonsPlugins.autoImport._
import com.giogar.aws.builder.AwsClientBuilder
import com.giogar.aws.credentials.AwsCredentialsProvider.toConvertibleCredentialsProvider
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

  def createStackTask() = Def.task {
    val awsRegion = (region in aws).value
    val awsCredentialsProvider = (credentialsProvider in aws).value
    val awsCloudformationConfigRoot = (configurationRootFolder in aws).value.getAbsolutePath + "/" + (cloudformationFolder in aws).value
    val stackName = (cloudformationStackName in aws).value
    val cloudformationTemplatePath = awsCloudformationConfigRoot + "/" + (cloudformationTemplateFilename in aws).value
    val capability = (capabilitiesIam in aws).value

    val stackRequest = new CreateStackRequest()
      .withStackName(stackName)
      .withCapabilities(Set(capability).asJava)
      .withTemplateBody(scala.io.Source.fromFile(cloudformationTemplatePath).mkString)

    val stackId = awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion, awsCredentialsProvider.toAws, null)
      .createStack(stackRequest)
      .getStackId

  }

  def updateStackTask() = Def.task {
    val awsRegion = (region in aws).value
    val awsCredentialsProvider = (credentialsProvider in aws).value
    val awsCloudformationConfigRoot = (configurationRootFolder in aws).value.getAbsolutePath + "/" + (cloudformationFolder in aws).value
    val stackName = (cloudformationStackName in aws).value
    val cloudformationTemplatePath = awsCloudformationConfigRoot + "/" + (cloudformationTemplateFilename in aws).value

    val capability = (capabilitiesIam in aws).value

    val stackRequest = new UpdateStackRequest()
      .withStackName(stackName)
      .withCapabilities(Set(capability).asJava)
      .withTemplateBody(scala.io.Source.fromFile(cloudformationTemplatePath).mkString)

    val stackId = awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion, awsCredentialsProvider.toAws, null)
      .updateStack(stackRequest)
      .getStackId

  }

  def deleteStackTask(): Def.Initialize[Task[Unit]] = Def.task {
    val awsRegion = (region in aws).value
    val awsCredentialsProvider = (credentialsProvider in aws).value
    val awsCloudformationConfigRoot = (configurationRootFolder in aws).value.getAbsolutePath + "/" + (cloudformationFolder in aws).value
    val stackName = (cloudformationStackName in aws).value
    val cloudformationTemplatePath = awsCloudformationConfigRoot + "/" + (cloudformationTemplateFilename in aws).value

    val stackRequest = new DeleteStackRequest()
      .withStackName(stackName)

    awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion, awsCredentialsProvider.toAws, null)
      .deleteStack(stackRequest)

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
