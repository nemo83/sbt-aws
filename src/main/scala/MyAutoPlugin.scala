import java.io.File

import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudformation.AmazonCloudFormationClient
import com.amazonaws.services.cloudformation.model.CreateStackRequest
import com.giogar.aws.AwsClientBuilder
import sbt.Keys._
import sbt._
import sbt.complete.DefaultParsers

object MyAutoPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  private val awsClientBuilder = new AwsClientBuilder

  object autoImport {

    val aws = taskKey[Unit]("aws")

    val region = settingKey[Regions]("aws-region")


    // AWS Common Configuration
    val awsConfigurationRootFolder = settingKey[File]("AWS Configuration root folder")

    // ** AWS Cloudformation **
    // Tasks
    val createStack = taskKey[Unit]("create-stack")

    // Settings
    val cloudformationStackName = settingKey[String]("AWS Cloudformation Stack name")
    val cloudformationFolder = settingKey[String]("AWS Cloudformation folder")
    val cloudformationTemplateFilename = settingKey[String]("AWS Cloudformation filename")

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

  def createStackTask() = Def.task {

    val awsRegion = (region in aws).value
    val awsCloudformationConfigRoot = (awsConfigurationRootFolder in aws).value.getAbsolutePath + "/" + (cloudformationFolder in aws).value
    val stackName = (cloudformationStackName in aws).value
    val cloudformationTemplatePath = awsCloudformationConfigRoot + "/" + (cloudformationTemplateFilename in aws).value

    println(s"CFN config root: ${awsCloudformationConfigRoot}")

    val stackRequest = new CreateStackRequest
    stackRequest.setStackName(stackName)
    stackRequest.setTemplateBody(cloudformationTemplatePath)

    val stackId = awsClientBuilder
      .createAWSClient(classOf[AmazonCloudFormationClient], awsRegion, null, null)
      .createStack(stackRequest)
      .getStackId

    println(s"stackId: $stackId")

  }

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
    deploy in aws <<= deployTask(),
    // AWS Cloudformation
    cloudformationStackName in aws := name.value,
    cloudformationFolder in aws := "cloudformation",
    cloudformationTemplateFilename in aws := "cloudformation.template",
    createStack in aws <<= createStackTask()
  )


}
