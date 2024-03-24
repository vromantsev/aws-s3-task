package ua.reed.aws.s3.dto;

import java.util.List;

public record AddTagsRequestDto(String bucketName, String objectKey, List<CustomTagDto> tags) {
}
