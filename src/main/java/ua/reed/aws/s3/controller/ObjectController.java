package ua.reed.aws.s3.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import ua.reed.aws.s3.dto.AddTagsRequestDto;
import ua.reed.aws.s3.dto.ObjectInfoDto;
import ua.reed.aws.s3.service.ObjectService;

@RestController
@RequestMapping("/api/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;

    @GetMapping
    public ResponseEntity<byte[]> downloadObject(@RequestParam("bucketName") final String bucketName,
                                                 @RequestParam("objectKey") final String objectKey,
                                                 final HttpServletResponse response) {
        ResponseBytes<GetObjectResponse> responseBytes = this.objectService.getObject(bucketName, objectKey);
        byte[] object = responseBytes.asByteArray();
        response.setContentLength(object.length);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(objectKey));
        return ResponseEntity.ok()
                .body(object);
    }

    @GetMapping("/secured")
    public ResponseEntity<byte[]> downloadObjectByPreSignedUrl(@RequestParam("bucketName") final String bucketName,
                                                               @RequestParam("objectKey") final String objectKey,
                                                               final HttpServletResponse response) {
        byte[] object = this.objectService.getObjectFromPreSignedUrl(bucketName, objectKey);
        response.setContentLength(object.length);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=".concat(objectKey));
        return ResponseEntity.ok()
                .body(object);
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    public void addTagsToObject(@RequestBody final AddTagsRequestDto request) {
        this.objectService.addTags(request);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void uploadObject(@RequestParam("bucketName") final String bucketName,
                             @RequestParam("objectKey") final String objectKey,
                             @RequestParam("file") final MultipartFile file) {
        this.objectService.createObject(bucketName, objectKey, file);
    }

    @PostMapping("/secured")
    @ResponseStatus(HttpStatus.CREATED)
    public void createObjectByPreSignedUrl(@RequestParam("bucketName") final String bucketName,
                                           @RequestParam("objectKey") final String objectKey,
                                           @RequestParam("file") final MultipartFile file) {
        this.objectService.createObjectByPreSignedUrl(bucketName, objectKey, file);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteObject(@RequestParam("bucketName") final String bucketName,
                             @RequestParam("objectKey") final String objectKey) {
        this.objectService.deleteObject(bucketName, objectKey);
    }

    @DeleteMapping("/secured")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteObjectByPreSignedUrl(@RequestParam("bucketName") final String bucketName,
                                           @RequestParam("objectKey") final String objectKey) {
        this.objectService.deleteObjectByPreSignedUrl(bucketName, objectKey);
    }

    @GetMapping("/info")
    public ResponseEntity<ObjectInfoDto> getObjectInfo(@RequestParam("bucketName") final String bucketName,
                                                       @RequestParam("objectKey") final String objectKey) {
        ObjectInfoDto objectInfo = this.objectService.getObjectInfo(bucketName, objectKey);
        return ResponseEntity.ok()
                .body(objectInfo);
    }
}
