package com.giogar.aws.credentials

sealed trait AwsCredentialsProvider

case class ProfileCredentialsProvider(profilesConfigFilePath: String = s"${sys.env("HOME")}/.aws/credentials", profileName: String = "default") extends AwsCredentialsProvider

trait ConvertibleCredentialsProvider {

  def awsCredentialsProvider: AwsCredentialsProvider

  def toAws: com.amazonaws.auth.AWSCredentialsProvider = {
    awsCredentialsProvider match {
      case ProfileCredentialsProvider(profilesConfigFilePath, profileName) => new com.amazonaws.auth.profile.ProfileCredentialsProvider(profilesConfigFilePath, profileName)
    }
  }

}

object AwsCredentialsProvider {

  implicit def toConvertibleCredentialsProvider(awsCredentialsProvider: AwsCredentialsProvider): ConvertibleCredentialsProvider = {
    new ConvertibleCredentialsProvider {
      override def awsCredentialsProvider: AwsCredentialsProvider = awsCredentialsProvider
    }
  }

}
