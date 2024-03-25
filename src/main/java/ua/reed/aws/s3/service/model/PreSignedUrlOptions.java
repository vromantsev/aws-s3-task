package ua.reed.aws.s3.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ua.reed.aws.s3.enums.PreSignedUrlOperationType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreSignedUrlOptions {

    private String bucketName;
    private String objectKey;
    private PreSignedUrlOperationType operationType;
    private MultipartFile multipartFile;
}
