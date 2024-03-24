package ua.reed.aws.s3.service;

import ua.reed.aws.s3.enums.PreSignedUrlOperationType;

import java.net.URL;

public interface PreSignedUrlService {

    URL generatePreSignedUrlForObject(final String bucketName, final String objectKey, final PreSignedUrlOperationType operationType);

}
