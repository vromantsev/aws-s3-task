package ua.reed.aws.s3.dto;

public record CreateBucketResponseDto(String bucketName, String requestId, String location) {

    public static CreateBucketResponseDto of(String bucketName, String requestId, String location) {
        return new CreateBucketResponseDto(bucketName, requestId, location);
    }
}
