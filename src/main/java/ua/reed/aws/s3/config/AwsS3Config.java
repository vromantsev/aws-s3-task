package ua.reed.aws.s3.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import software.amazon.awssdk.auth.credentials.InstanceProfileCredentialsProvider;
import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class AwsS3Config {

    private static final String PROFILE_KEY = "aws.profile";

    private final Environment environment;

    @Profile("local")
    @Bean
    public S3Client localS3Client() {
        return getS3ClientBuilder()
                .credentialsProvider(ProfileCredentialsProvider.create(environment.getProperty(PROFILE_KEY)))
                .build();
    }

    @Profile("ec2")
    @Bean
    public S3Client ec2S3Client() {
        return getS3ClientBuilder()
                .credentialsProvider(InstanceProfileCredentialsProvider.create())
                .build();
    }

    @Profile("local")
    @Bean
    public S3Presigner localS3Presigner() {
        return getS3PresignedBuilder()
                .credentialsProvider(ProfileCredentialsProvider.create(environment.getProperty(PROFILE_KEY)))
                .build();
    }

    @Profile("ec2")
    @Bean
    public S3Presigner ec2S3Presigner() {
        return getS3PresignedBuilder()
                .credentialsProvider(ProfileCredentialsProvider.create(environment.getProperty(PROFILE_KEY)))
                .build();
    }

    private S3Presigner.Builder getS3PresignedBuilder() {
        return S3Presigner.builder()
                .region(Region.EU_NORTH_1);
    }

    private S3ClientBuilder getS3ClientBuilder() {
        return S3Client.builder()
                .region(Region.EU_NORTH_1);
    }
}
