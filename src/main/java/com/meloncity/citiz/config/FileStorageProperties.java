package com.meloncity.citiz.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "file")
@Getter
public class FileStorageProperties {
    private String storage; // local / s3

    private Local local;
    private S3 s3;

    FileStorageProperties(String storage, Local local, S3 s3) {
        this.storage = storage;
        this.local = local;
        this.s3 = s3;
    }

    @Getter
    public static class Local {
        private String baseDir;

        Local(String baseDir){
            this.baseDir = baseDir;
        }
    }

    @Getter
    public static class S3 {
        private String bucket;
        private String baseDir;

        S3(String bucket, String baseDir){
            this.bucket = bucket;
            this.baseDir = baseDir;
        }
    }
}
