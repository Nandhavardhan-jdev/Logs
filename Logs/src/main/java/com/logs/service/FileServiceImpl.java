package com.logs.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileServiceImpl implements FileService {

	public InputStreamResource errorLevel(MultipartFile file) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			File file2 = new File("C:\\Users\\Admin\\Desktop\\Logs\\outputs\\"+file.getOriginalFilename());
			BufferedWriter writer = new BufferedWriter(new FileWriter(file2));
			String line;
			boolean data = false;
			while ((line=reader.readLine()) != null) {
				if (line.contains("ERROR")) {
					data = true;
					String[] words = line.split(" ");
					writer.write("Time	: "+words[0]+"\n"+"UserId	: "+words[3]+"\n"+"URL	: "+words[4]+"\n"+"Error	: "+reader.readLine()+"\n"+reader.readLine()+"\n"+reader.readLine()+"\n"+reader.readLine()+"\n"+"\n");
				}
			}
			reader.close();
			writer.close();
			if (data) {
				InputStreamResource isr = new InputStreamResource(new FileInputStream(file2));
				return isr;
			}
			else throw new RuntimeException("not found");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}
	}

	
}
