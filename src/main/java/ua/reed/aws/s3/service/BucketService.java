package ua.reed.aws.s3.service;

import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import ua.reed.aws.s3.dto.CreateBucketResponseDto;
import ua.reed.aws.s3.dto.GetBucketResponseDto;
import ua.reed.aws.s3.dto.BucketInfoDto;

public interface BucketService {

    CreateBucketResponseDto createBucket(final String bucketName);

    GetBucketResponseDto getBucket(final String bucketName);

    void updateBucketVersioning(final String bucketName, final BucketVersioningStatus bucketVersioningStatus);

    void deleteBucket(final String bucketName);

    BucketInfoDto getBucketInfo(final String bucketName);

}
