package com.logs.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.logs.dto.FileDto;
import com.logs.service.FileService;

@RestController
public class FileController {
	
	@Autowired
	FileService fileService;
	
	@GetMapping(value = "/error",headers = "content-type=multipart/*")
	public ResponseEntity<InputStreamResource> errorLevel(@RequestParam("file") MultipartFile file) {
		if (file.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
		InputStreamResource isr = fileService.errorLevel(file);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		httpHeaders.setContentDispositionFormData("attachment", file.getOriginalFilename());
		return new ResponseEntity<>(isr, httpHeaders, HttpStatus.OK);
	}
	
	@GetMapping("/error")
	public ResponseEntity<InputStreamResource> errorLevel(@RequestBody FileDto fileDto) {
		InputStreamResource isr = fileService.errorLevel(fileDto);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		String fileName = "titan-"+fileDto.getServerName()+"."+fileDto.getDate()+".log";
		httpHeaders.setContentDispositionFormData("attachment", fileName);
		return new ResponseEntity<>(isr, httpHeaders, HttpStatus.OK);
	}
	
	@GetMapping("/error/multi")
	public ResponseEntity<InputStreamResource> errorLevelMulti(@RequestBody FileDto fileDto) {
		InputStreamResource isr = fileService.errorLevelMulti(fileDto);
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		String fileName = "titan-"+fileDto.getServerNames()+"."+fileDto.getDates()+".log";
		httpHeaders.setContentDispositionFormData("attachment", fileName);
		return new ResponseEntity<>(isr, httpHeaders, HttpStatus.OK);
	}
	
}
