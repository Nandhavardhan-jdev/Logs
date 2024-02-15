package com.logs.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class FileServiceImpl implements FileService {
	
	@Autowired
	JavaMailSender javaMailSender;

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
					writer.write("Time	: "+words[0]+"\n"+"Pwd	: "+words[3]+"\n"+"URL	: "+words[4]+"\n"+"Error	: "+words[8]+" "+words[9]+" "+words[10]+" "+words[11]+"\n"+"	"+reader.readLine()+"\n"+reader.readLine()+"\n"+reader.readLine()+"\n"+reader.readLine()+"\n"+"\n");
				}
			}
			reader.close();
			writer.close();
			if (data) {
				InputStreamResource isr = new InputStreamResource(new FileInputStream(file2));
				this.sendMailWithAttachment(file2);
				return isr;
			}
			else throw new RuntimeException("not found");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}
	}

	public void sendMailWithAttachment(File file) {
		
		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			mimeMessageHelper.setTo("mail@gmail.com");
			String[] cc = {"mail1@gmail.com","mail2@gmail.com"};
			mimeMessageHelper.setCc(cc);
			mimeMessageHelper.setSubject("Email Generation");
			mimeMessageHelper.setText("Hi All,\n\n"
					+ "This mail is generated through smtp with attachment."
					+ "\n\nThanks & Regards\nG Nandhavardhan Reddy");
			FileSystemResource fileSystemResource = new FileSystemResource(file);
			mimeMessageHelper.addAttachment(fileSystemResource.getFilename(), fileSystemResource);
			javaMailSender.send(mimeMessage);
			System.out.println("mail sended");
		} catch (MessagingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("not sended");
		}
		
	}
	
}
