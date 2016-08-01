package com.giogar.aws.builder

import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.{AmazonWebServiceClient, ClientConfiguration}

class AwsClientBuilder {

  def createAWSClient[T <: AmazonWebServiceClient](clazz: Class[T],
                                                   regions: Regions,
                                                   credentialsProvider: AWSCredentialsProvider,
                                                   clientConfiguration: ClientConfiguration): T = {
    Region.getRegion(regions).createClient(clazz, credentialsProvider, clientConfiguration)
  }

}
