package ua.reed.aws.s3.dto;

import lombok.Builder;

import java.time.Instant;

@Builder
public record ObjectInfoDto(String objectKey,
                            String contentType,
                            Long contentLength,
                            String eTag,
                            String expiration,
                            Instant expires) {
}
