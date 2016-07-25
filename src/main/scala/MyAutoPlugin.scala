import sbt._

object MyAutoPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val aws = taskKey[Unit]("aws")

    val awsConfigurationFolder = taskKey[String]("aws-configuration-root-folder")

    val createStack = taskKey[Unit]("create-stack")

    val region = settingKey[String]("aws-region")

  }

  import autoImport._

  def createStackTask() = Def.task {
    println(s"using region: ${region.value}")
  }

//  override def buildSettings: Seq[Def.Setting[_]] = Seq (
//    region := "us-east-1",
//    createStack in aws <<= createStackTask()
//  )

  override def projectSettings: Seq[Def.Setting[_]] = Seq (
    createStack in aws <<= createStackTask()
  )

  override def globalSettings: Seq[Def.Setting[_]] = Seq (
    region := "us-east-1"
  )

}
