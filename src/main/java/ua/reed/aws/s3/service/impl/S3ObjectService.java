package ua.reed.aws.s3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpExecuteRequest;
import software.amazon.awssdk.http.HttpExecuteResponse;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.SdkHttpMethod;
import software.amazon.awssdk.http.SdkHttpRequest;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectTaggingRequest;
import software.amazon.awssdk.services.s3.model.Tag;
import software.amazon.awssdk.services.s3.model.Tagging;
import ua.reed.aws.s3.dto.AddTagsRequestDto;
import ua.reed.aws.s3.dto.CustomTagDto;
import ua.reed.aws.s3.dto.ObjectInfoDto;
import ua.reed.aws.s3.enums.PreSignedUrlOperationType;
import ua.reed.aws.s3.service.ObjectService;
import ua.reed.aws.s3.service.PreSignedUrlService;
import ua.reed.aws.s3.utils.S3Utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ObjectService implements ObjectService {

    private final S3Client s3Client;
    private final PreSignedUrlService preSignedUrlService;

    @Override
    public void createObject(final String bucketName, final String objectKey, final MultipartFile multipartFile) {
        S3Utils.validateBucketName(bucketName);
        S3Utils.validateObjectKey(objectKey);
        try {
            this.s3Client.putObject(
                    PutObjectRequest.builder()
                            .key(objectKey)
                            .bucket(bucketName)
                            .build(),
                    RequestBody.fromBytes(multipartFile.getBytes())
            );
            log.info("File '{}' successfully uploaded to S3 bucket {}", multipartFile.getOriginalFilename(), bucketName);
        } catch (IOException ex) {
            throw new RuntimeException("Cannot upload a file '%s' to S3 bucket '%s'".formatted(multipartFile.getOriginalFilename(), bucketName), ex);
        }
    }

    @Override
    public void createObjectByPreSignedUrl(final String bucketName, final String objectKey, final MultipartFile multipartFile) {
        S3Utils.validateBucketName(bucketName);
        S3Utils.validateObjectKey(objectKey);
        URL preSignedUrl = this.preSignedUrlService.generatePreSignedUrlForObject(bucketName, objectKey, PreSignedUrlOperationType.DELETE_OBJECT);
        try (SdkHttpClient httpClient = ApacheHttpClient.create()) {
            HttpExecuteRequest request = HttpExecuteRequest.builder()
                    .request(
                            SdkHttpRequest.builder()
                                    .method(SdkHttpMethod.PUT)
                                    .uri(preSignedUrl.toURI())
                                    .build()
                    )
                    .contentStreamProvider(RequestBody.fromBytes(multipartFile.getBytes()).contentStreamProvider())
                    .build();
            HttpExecuteResponse response = httpClient.prepareRequest(request).call();
            SdkHttpResponse sdkHttpResponse = response.httpResponse();
            if (sdkHttpResponse.statusCode() == 200) {
                log.info("Successfully created object with key={}, bucket={}", objectKey, bucketName);
            } else {
                log.error("Cannot upload file. Status: {}, details: {}", sdkHttpResponse.statusCode(), sdkHttpResponse.statusText().get());
            }
        } catch (URISyntaxException | IOException ex) {
            throw new RuntimeException("Cannot create object with key='%s' in bucket='%s'".formatted(objectKey, bucketName), ex);
        }
    }

    @Override
    public void addTags(final AddTagsRequestDto request) {
        String bucketName = request.bucketName();
        S3Utils.validateBucketName(bucketName);
        String objectKey = request.objectKey();
        S3Utils.validateObjectKey(objectKey);
        List<CustomTagDto> tags = request.tags();
        if (tags.isEmpty()) {
            log.warn("Received empty tags for bucket={}, objectKey={}, operation will be skipped", bucketName, objectKey);
        } else {
            try {
                List<Tag> targetCompatibleTags = tags.stream()
                        .map(t -> Tag.builder()
                                .key(t.key())
                                .value(t.value())
                                .build()
                        )
                        .toList();
                this.s3Client.putObjectTagging(
                        PutObjectTaggingRequest.builder()
                                .bucket(bucketName)
                                .key(objectKey)
                                .tagging(Tagging.builder().tagSet(targetCompatibleTags).build())
                                .build()
                );
                log.info("Added tags for object={}, bucket={}", objectKey, bucketName);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to add tags for objectKey='%s', bucket='%s'".formatted(objectKey, bucketName), ex);
            }
        }
    }

    @Override
    public ResponseBytes<GetObjectResponse> getObject(final String bucketName, final String objectKey) {
        S3Utils.validateBucketName(bucketName);
        S3Utils.validateObjectKey(objectKey);
        try {
            return this.s3Client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(objectKey)
                            .build()
            );
        } catch (Exception ex) {
            throw new RuntimeException("Cannot download object by key='%s' from bucket='%s'".formatted(objectKey, bucketName), ex);
        }
    }

    @Override
    public byte[] getObjectFromPreSignedUrl(final String bucketName, final String objectKey) {
        S3Utils.validateBucketName(bucketName);
        S3Utils.validateObjectKey(objectKey);
        URL preSignedUrl = this.preSignedUrlService.generatePreSignedUrlForObject(bucketName, objectKey, PreSignedUrlOperationType.GET_OBJECT);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (SdkHttpClient httpClient = ApacheHttpClient.create()) {
            HttpExecuteRequest request = HttpExecuteRequest.builder()
                    .request(
                            SdkHttpRequest.builder()
                                    .method(SdkHttpMethod.GET)
                                    .uri(preSignedUrl.toURI())
                                    .build()
                    )
                    .build();
            HttpExecuteResponse response = httpClient.prepareRequest(request).call();
            response.responseBody()
                    .ifPresentOrElse(abortableInputStream -> {
                                try {
                                    baos.writeBytes(abortableInputStream.readAllBytes());
                                } catch (IOException ex) {
                                    throw new RuntimeException("Cannot copy a file", ex);
                                }
                            },
                            () -> log.error("Cannot download a file as no response body is received.")
                    );
            return baos.toByteArray();
        } catch (URISyntaxException | IOException ex) {
            throw new RuntimeException("Cannot download a file by pre-signed url", ex);
        }
    }

    @Override
    public void deleteObject(final String bucketName, final String objectKey) {
        S3Utils.validateBucketName(bucketName);
        S3Utils.validateObjectKey(objectKey);
        try {
            this.s3Client.deleteObject(
                    DeleteObjectRequest.builder()
                            .key(objectKey)
                            .bucket(bucketName)
                            .build()
            );
            log.info("Object={} was deleted from bucket={}", objectKey, bucketName);
        } catch (Exception ex) {
            throw new RuntimeException("Cannot delete object='%s' from bucket='%s'".formatted(objectKey, bucketName));
        }
    }

    @Override
    public void deleteObjectByPreSignedUrl(final String bucketName, final String objectKey) {
        S3Utils.validateBucketName(bucketName);
        S3Utils.validateObjectKey(objectKey);
        URL preSignedUrl = this.preSignedUrlService.generatePreSignedUrlForObject(bucketName, objectKey, PreSignedUrlOperationType.DELETE_OBJECT);
        try (SdkHttpClient httpClient = ApacheHttpClient.create()) {
            HttpExecuteRequest request = HttpExecuteRequest.builder()
                    .request(
                            SdkHttpRequest.builder()
                                    .method(SdkHttpMethod.DELETE)
                                    .uri(preSignedUrl.toURI())
                                    .build()
                    )
                    .build();
            HttpExecuteResponse response = httpClient.prepareRequest(request).call();
            response.responseBody()
                    .ifPresent(abortableInputStream -> log.info("Object={} successfully deleted from bucket={} by pre-signed url", objectKey, bucketName));
        } catch (URISyntaxException | IOException ex) {
            throw new RuntimeException("Cannot delete object='%s' from bucket='%s' by pre-signed url".formatted(objectKey, bucketName), ex);
        }
    }

    @Override
    public ObjectInfoDto getObjectInfo(final String bucketName, final String objectKey) {
        S3Utils.validateBucketName(bucketName);
        S3Utils.validateObjectKey(objectKey);
        try {
            HeadObjectResponse response = this.s3Client.headObject(
                    HeadObjectRequest.builder()
                            .key(objectKey)
                            .bucket(bucketName)
                            .build()
            );
            return ObjectInfoDto.builder()
                    .objectKey(objectKey)
                    .contentType(response.contentType())
                    .contentLength(response.contentLength())
                    .eTag(response.eTag())
                    .expiration(response.expiration())
                    .expires(response.expires())
                    .build();
        } catch (Exception ex) {
            throw new RuntimeException("Cannot get info for objectKey='%s' from bucket='%s'".formatted(objectKey, bucketName), ex);
        }
    }
}
