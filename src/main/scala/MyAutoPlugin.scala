import com.amazonaws.regions.Regions
import sbt._

object MyAutoPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  object autoImport {

    val aws = taskKey[Unit]("aws")

    val createStack = taskKey[Unit]("create-stack")

    val region = settingKey[Regions]("aws-region")

  }

  import autoImport._

  def createStackTask() = Def.task {
    val awsRegion = (region in aws).value
    println(s"using region: $awsRegion")
  }


  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    region in aws := Regions.US_EAST_1,
    createStack in aws <<= createStackTask()
  )


}
