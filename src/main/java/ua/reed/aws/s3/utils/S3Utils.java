package ua.reed.aws.s3.utils;

import lombok.experimental.UtilityClass;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.DeleteObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@UtilityClass
public class S3Utils {

    public void validateBucketName(String bucketName) {
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("Parameter [bucketName] must not be null or empty!");
        }
    }

    public void validateObjectKey(String objectKey) {
        if (objectKey == null || objectKey.isEmpty()) {
            throw new IllegalArgumentException("Parameter [objectKey] must not be null or empty!");
        }
    }

    public GetObjectPresignRequest buildPreSignedGetObjectRequest(String bucketName, String objectKey) {
        return GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .getObjectRequest(
                        GetObjectRequest.builder()
                                .key(objectKey)
                                .bucket(bucketName)
                                .build()
                )
                .build();
    }

    public PutObjectPresignRequest buildPreSignedPutObjectRequest(String bucketName, String objectKey) {
        return PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .putObjectRequest(
                        PutObjectRequest.builder()
                                .key(objectKey)
                                .bucket(bucketName)
                                .build()
                )
                .build();
    }

    public DeleteObjectPresignRequest buildPreSignedDeleteObjectRequest(String bucketName, String objectKey) {
        return DeleteObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10))
                .deleteObjectRequest(
                        DeleteObjectRequest.builder()
                                .key(objectKey)
                                .bucket(bucketName)
                                .build()
                )
                .build();
    }
}
