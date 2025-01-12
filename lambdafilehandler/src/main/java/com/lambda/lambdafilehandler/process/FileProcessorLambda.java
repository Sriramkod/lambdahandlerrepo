package com.lambda.lambdafilehandler.process;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

public class FileProcessorLambda {

	/*
	 * @Autowired private S3Client s3Client;
	 */

	// packageName.className::methodName
	// above, after uploading lambda...
	
	// now create a role for aws lambda...
	// add permission - attach inline policy...
	/* add the below policy, for the IAM role.. to read s3 object
	 
	
	   
	   {
  "Version": "2025-01-12",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::my-spring-boot-app-bucket-ram/*"
    }
  ]
}

	 *  
	 *  */
	 //we need AmazonS3Client to read the contents from S3 object
    private static final AmazonS3 s3Client = AmazonS3Client.builder()
            .withCredentials(new DefaultAWSCredentialsProviderChain())
            .build();
    public Boolean handleRequest(S3Event s3event, Context context) {

        final LambdaLogger logger = context.getLogger();

        //logic to check if any records found
        if(s3event.getRecords().isEmpty()){
            logger.log("No records found");
            return false;
        }
        //process the records
        for(S3EventNotification.S3EventNotificationRecord record: s3event.getRecords()){
            String bucketName = record.getS3().getBucket().getName();
            String objectKey = record.getS3().getObject().getKey();

            com.amazonaws.services.s3.model.S3Object s3Object = s3Client.getObject(bucketName, objectKey);
            S3ObjectInputStream inputStream = s3Object.getObjectContent();

            try{
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                br.lines().forEach(line -> logger.log(line + "\n"));
            } catch (Exception e){
                logger.log("Error occurred while processing:" + e.getMessage());
                return false;
            }
        }
        return true;
    }
}