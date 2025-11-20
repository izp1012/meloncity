package com.meloncity.citiz.util.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(MultipartFile file) throws Exception;
}
