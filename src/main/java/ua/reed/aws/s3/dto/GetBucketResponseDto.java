package ua.reed.aws.s3.dto;

import java.time.ZonedDateTime;

public record GetBucketResponseDto(String bucketName, ZonedDateTime creationDate) {

    public static GetBucketResponseDto of(String bucketName, ZonedDateTime creationDate) {
        return new GetBucketResponseDto(bucketName, creationDate);
    }
}
