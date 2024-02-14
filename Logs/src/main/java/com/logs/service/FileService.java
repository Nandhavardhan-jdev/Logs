package com.logs.service;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	InputStreamResource errorLevel(MultipartFile file);

}
