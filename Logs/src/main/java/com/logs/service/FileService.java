package com.logs.service;

import java.util.List;

import org.springframework.core.io.InputStreamResource;
import org.springframework.web.multipart.MultipartFile;

import com.logs.dto.FileDto;

public interface FileService {

	InputStreamResource errorLevel(MultipartFile file);

	InputStreamResource errorLevel(FileDto fileDto);

	InputStreamResource errorLevelMulti(FileDto fileDto);

	void downloadFiles(FileDto fileDto);

}
