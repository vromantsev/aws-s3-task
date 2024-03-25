package ua.reed.aws.s3.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedDeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import ua.reed.aws.s3.service.PreSignedUrlService;
import ua.reed.aws.s3.service.model.PreSignedUrlOptions;
import ua.reed.aws.s3.utils.S3Utils;

import java.net.URL;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3PreSignedUrlService implements PreSignedUrlService {

    private final S3Presigner s3Presigner;

    @Override
    public URL generatePreSignedUrlForObject(final PreSignedUrlOptions options) {
        String bucketName = options.getBucketName();
        S3Utils.validateBucketName(bucketName);
        String objectKey = options.getObjectKey();
        S3Utils.validateObjectKey(objectKey);
        try {
            URL preSignedUrl = null;
            switch (options.getOperationType()) {
                case GET_OBJECT -> {
                    PresignedGetObjectRequest request = s3Presigner.presignGetObject(S3Utils.buildPreSignedGetObjectRequest(bucketName, objectKey));
                    preSignedUrl = request.url();
                }
                case PUT_OBJECT -> {
                    PresignedPutObjectRequest request = s3Presigner.presignPutObject(S3Utils.buildPreSignedPutObjectRequest(options));
                    preSignedUrl = request.url();
                }
                case DELETE_OBJECT -> {
                    PresignedDeleteObjectRequest request = s3Presigner.presignDeleteObject(S3Utils.buildPreSignedDeleteObjectRequest(bucketName, objectKey));
                    preSignedUrl = request.url();
                }
            }
            log.info(
                    "Generated pre-signed url for objectKey={}, bucket={}, result={}",
                    objectKey,
                    bucketName,
                    Objects.requireNonNull(preSignedUrl, "Pre-signed url must not be null!").toExternalForm()
            );
            return preSignedUrl;
        } catch (Exception ex) {
            throw new RuntimeException("Cannot generate pre-signed url for objectKey='%s', bucket='%s'".formatted(objectKey, bucketName), ex);
        }
    }
}
