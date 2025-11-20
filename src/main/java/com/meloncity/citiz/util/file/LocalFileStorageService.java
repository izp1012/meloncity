package com.meloncity.citiz.util.file;

import com.meloncity.citiz.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileStorageService implements FileStorageService{

    private final FileStorageProperties properties;

    @Override
    public String upload(MultipartFile file) throws Exception {
        String baseDir = properties.getLocal().getBaseDir();
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();

        Path path = Paths.get(baseDir, fileName);

        Files.createDirectories(path.getParent());
        file.transferTo(path.toFile());

        return fileName; // 프론트에 사용할 URL
    }
}
