package ua.reed.aws.s3.dto;

import lombok.Builder;

@Builder
public record BucketInfoDto(String bucketName, String bucketRegion, String bucketLocationName, String locationType) {
}
