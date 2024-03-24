package ua.reed.aws.s3.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.model.BucketVersioningStatus;
import ua.reed.aws.s3.dto.CreateBucketRequestDto;
import ua.reed.aws.s3.dto.CreateBucketResponseDto;
import ua.reed.aws.s3.dto.GetBucketResponseDto;
import ua.reed.aws.s3.dto.BucketInfoDto;
import ua.reed.aws.s3.service.BucketService;

@RestController
@RequestMapping("/api/buckets")
@RequiredArgsConstructor
public class BucketController {

    private final BucketService bucketService;

    @PostMapping
    public ResponseEntity<CreateBucketResponseDto> createBucket(@RequestBody final CreateBucketRequestDto request) {
        return ResponseEntity.ok()
                .body(this.bucketService.createBucket(request.bucketName()));
    }

    @GetMapping
    public ResponseEntity<GetBucketResponseDto> getBucketByName(@RequestParam("bucketName") final String bucketName) {
        return ResponseEntity.ok()
                .body(this.bucketService.getBucket(bucketName));
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public void updateBucketVersioning(@RequestParam("bucketName") final String bucketName,
                                       @RequestParam("status") final String status) {
        this.bucketService.updateBucketVersioning(bucketName, BucketVersioningStatus.fromValue(status));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBucket(@RequestParam("bucketName") final String bucketName) {
        this.bucketService.deleteBucket(bucketName);
    }

    @GetMapping("/info")
    public ResponseEntity<BucketInfoDto> getBucketInfo(@RequestParam("bucketName") final String bucketName) {
        BucketInfoDto result = this.bucketService.getBucketInfo(bucketName);
        return ResponseEntity.ok()
                .body(result);
    }
}
