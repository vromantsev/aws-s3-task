package ua.reed.aws.s3.service;

import ua.reed.aws.s3.enums.PreSignedUrlOperationType;
import ua.reed.aws.s3.service.model.PreSignedUrlOptions;

import java.net.URL;

public interface PreSignedUrlService {

    URL generatePreSignedUrlForObject(final PreSignedUrlOptions options);

}
