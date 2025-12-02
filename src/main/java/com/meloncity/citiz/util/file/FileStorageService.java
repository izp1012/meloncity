package com.meloncity.citiz.util.file;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileStorageService {
    String upload(MultipartFile file) throws IOException;
    String getBaseDir();
    void delete(String fileName) throws IOException;
}
