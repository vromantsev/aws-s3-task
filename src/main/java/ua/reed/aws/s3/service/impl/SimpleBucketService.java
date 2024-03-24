package ua.reed.aws.s3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.CreateBucketResponse;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteBucketResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.HeadBucketResponse;
import software.amazon.awssdk.services.s3.model.PutBucketVersioningRequest;
import software.amazon.awssdk.services.s3.model.VersioningConfiguration;
import ua.reed.aws.s3.dto.CreateBucketResponseDto;
import ua.reed.aws.s3.dto.GetBucketResponseDto;
import ua.reed.aws.s3.dto.BucketInfoDto;
import ua.reed.aws.s3.service.BucketService;
import ua.reed.aws.s3.utils.S3Utils;

import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleBucketService implements BucketService {

    private final S3Client s3Client;

    @Override
    public CreateBucketResponseDto createBucket(final String bucketName) {
        S3Utils.validateBucketName(bucketName);
        try {
            CreateBucketResponse response = this.s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            log.info("Created a bucket '{}'", bucketName);
            return CreateBucketResponseDto.of(bucketName, response.responseMetadata().requestId(), response.location());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create a bucket '%s'".formatted(bucketName), ex);
        }
    }

    @Override
    public GetBucketResponseDto getBucket(final String bucketName) {
        S3Utils.validateBucketName(bucketName);
        try {
            Bucket bucket = this.s3Client.listBuckets()
                    .buckets()
                    .stream()
                    .filter(b -> b.name().equals(bucketName))
                    .findAny()
                    .orElseThrow();
            return GetBucketResponseDto.of(bucket.name(), bucket.creationDate().atZone(ZoneId.systemDefault()));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to get bucket '%s'".formatted(bucketName), ex);
        }
    }

    @Override
    public void updateBucketVersioning(final String bucketName, final BucketVersioningStatus bucketVersioningStatus) {
        S3Utils.validateBucketName(bucketName);
        try {
            this.s3Client.putBucketVersioning(PutBucketVersioningRequest.builder()
                    .bucket(bucketName)
                    .versioningConfiguration(
                            VersioningConfiguration.builder().status(bucketVersioningStatus).build()
                    )
                    .build()
            );
            log.info("Updated versioning for bucket '{}' with value '{}'", bucketName, bucketVersioningStatus.name());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to update versioning for bucket '%s'".formatted(bucketName), ex);
        }
    }

    @Override
    public void deleteBucket(final String bucketName) {
        S3Utils.validateBucketName(bucketName);
        try {
            DeleteBucketResponse response = this.s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(bucketName).build());
            log.info("Deleted a bucket '{}', status: {}", bucketName, response.sdkHttpResponse().statusCode());
        } catch (Exception ex) {
            throw new RuntimeException("Failed to delete a bucket '%s'".formatted(bucketName));
        }
    }

    @Override
    public BucketInfoDto getBucketInfo(final String bucketName) {
        S3Utils.validateBucketName(bucketName);
        try {
            HeadBucketResponse response = this.s3Client.headBucket(
                    HeadBucketRequest.builder()
                            .bucket(bucketName)
                            .build()
            );
            return BucketInfoDto.builder()
                    .bucketName(bucketName)
                    .bucketRegion(response.bucketRegion())
                    .bucketLocationName(response.bucketLocationName())
                    .locationType(response.bucketLocationTypeAsString())
                    .build();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot get info for bucket='%s'".formatted(bucketName), ex);
        }
    }
}
