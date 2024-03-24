package ua.reed.aws.s3.service;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import ua.reed.aws.s3.dto.AddTagsRequestDto;
import ua.reed.aws.s3.dto.ObjectInfoDto;

public interface ObjectService {

    void createObject(final String bucketName, final String objectKey, final MultipartFile multipartFile);

    void createObjectByPreSignedUrl(final String bucketName, final String objectKey, final MultipartFile multipartFile);

    void addTags(final AddTagsRequestDto request);

    ResponseBytes<GetObjectResponse> getObject(final String bucketName, final String objectKey);

    byte[] getObjectFromPreSignedUrl(final String bucketName, final String objectKey);

    void deleteObject(final String bucketName, final String objectKey);

    void deleteObjectByPreSignedUrl(final String bucketName, final String objectKey);

    ObjectInfoDto getObjectInfo(final String bucketName, final String objectKey);

}
