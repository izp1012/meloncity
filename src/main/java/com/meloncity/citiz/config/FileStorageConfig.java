package com.meloncity.citiz.config;

import com.meloncity.citiz.util.file.FileStorageService;
import com.meloncity.citiz.util.file.LocalFileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FileStorageProperties.class)
@RequiredArgsConstructor
public class FileStorageConfig {

    private final FileStorageProperties properties;
    private final ApplicationContext ctx;

    @Bean
    public FileStorageService fileStorageService() {
        String storage = properties.getStorage();

        if ("s3".equalsIgnoreCase(storage)) {
            //return ctx.getBean(S3FileStorageService.class);
            return ctx.getBean(LocalFileStorageService.class);
        } else {
            return ctx.getBean(LocalFileStorageService.class);
        }
    }
}
