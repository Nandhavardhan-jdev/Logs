package com.logs.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.logs.dto.FileDto;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	JavaMailSender javaMailSender;

	boolean data;

	public InputStreamResource errorLevel(MultipartFile file) {
		try {
			data = false;
			File file2 = new File("C:\\Users\\Admin\\Desktop\\Logs\\outputs\\" + file.getOriginalFilename());
			BufferedWriter writer = new BufferedWriter(new FileWriter(file2));
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
			String line = reader.readLine();
			while (line != null) {

				boolean nextLine = true;
				if ((line.contains("SimpleAsyncTaskExecutor") && line.contains("ERROR"))
						|| (line.contains("scheduling") && line.contains("ERROR"))
						|| (line.contains("main") && line.contains("ERROR"))) {
					this.data = true;
					String[] words = line.split(" ");
					writer.write("Time	: " + words[0] + "\nThread	: " + words[1] + "\nError	: ");
					for (int i = 5; i < words.length; i++) {
						writer.write(words[i] + " ");
					}
					for (int i = 0; i < 4; i++) {
						if ((line = reader.readLine()) != null) {
							if (line.contains("INFO") || line.contains("WARN") || line.contains("ERROR")
									|| line.contains("DEBUG") || line.contains("FATAL") || line.contains("TRACE")) {
								nextLine = false;
								break;
							}
							writer.write("\n	" + line);
						}
					}
					writer.write("\n\n");
				}

				else if (line.contains("setar") && line.contains("ERROR")) {
					data = true;
					String[] words = line.split(" ");
					writer.write("Time	: " + words[0] + "\nUserName	: " + words[2] + "\nURL	: " + words[4]
							+ "\nError	: ");
					for (int i = 8; i < words.length; i++) {
						writer.write(words[i] + " ");
					}
					for (int i = 0; i < 18; i++) {
						writer.write("\n" + "	" + reader.readLine());
					}
					writer.write("\n\n");
				}

				else if (line.contains("ERROR")) {
					data = true;
					String[] words = line.split(" ");
					writer.write("Time	: " + words[0] + "\nUserId	: " + words[2] + "\nURL	: " + words[4]
							+ "\nError	: ");
					for (int i = 8; i < words.length; i++) {
						writer.write(words[i] + " ");
					}
					for (int i = 0; i < 4; i++) {
						if ((line = reader.readLine()) != null) {
							if (line.contains("INFO") || line.contains("WARN") || line.contains("ERROR")
									|| line.contains("DEBUG") || line.contains("FATAL") || line.contains("TRACE")) {
								nextLine = false;
								break;
							}
							writer.write("\n	" + line);
						}
					}
					writer.write("\n\n");
				}
				if (nextLine)
					line = reader.readLine();

			}
			reader.close();
			writer.close();
			if (data) {
				InputStreamResource isr = new InputStreamResource(new FileInputStream(file2));
				return isr;
			} else
				throw new RuntimeException("not found\nno errors");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}
	}

	public InputStreamResource errorLevel(FileDto fileDto) {
		data = false;
		String serverName = fileDto.getServerName();
		String date = fileDto.getDate();
		List<String> paths = searchFiles(serverName, date);
		try {
			if (!paths.isEmpty()) {
				File file = new File(
						"C:\\Users\\Admin\\Desktop\\Logs\\output2\\" + "titan-" + serverName + "." + date + ".log");
				BufferedWriter writer;
				writer = new BufferedWriter(new FileWriter(file));
				paths.forEach(path -> writeInFile(new File(path), writer));
				writer.close();
				if (this.data) {
					InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
					if (!fileDto.getEmailTo().isEmpty()) {
						this.sendMailWithAttachment(file, fileDto);
					}
					return isr;
				} else {
					file.delete();
					throw new RuntimeException("no errors");
				}
			} else
				throw new RuntimeException("no files");
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}

	}

	public InputStreamResource errorLevelMulti(FileDto fileDto) {
		data = false;
		List<String> serverNames = fileDto.getServerNames();
		List<String> dates = fileDto.getDates();
		Iterator<String> sIterator = serverNames.iterator();
		Iterator<String> dIterator = dates.iterator();
		try {
			File file = new File(
					"C:\\Users\\Admin\\Desktop\\Logs\\output2\\" + "titan-" + serverNames + "." + dates + ".log");
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(file));
			while (sIterator.hasNext()) {
				List<String> paths = searchFiles(sIterator.next(), dIterator.next());
				if (!paths.isEmpty()) {
					paths.forEach(path -> writeInFile(new File(path), writer));
				}
			}
			writer.close();
			if (this.data) {
				InputStreamResource isr = new InputStreamResource(new FileInputStream(file));
				if (!fileDto.getEmailTo().isEmpty()) {
					this.sendMailWithAttachment(file, fileDto);
				}
				return isr;
			} else {
				file.delete();
				throw new RuntimeException("no errors");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}
	}

	public List<String> searchFiles(String serverName, String date) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<String> paths;
		try {
			if (date.contentEquals(currentDate.format(formatter))) {
				paths = Files.list(Paths.get("C:\\Users\\Admin\\Desktop\\Logs\\All logs")).map(Path::toString)
						.filter(file -> file.contains("titan-" + serverName + ".log")).collect(Collectors.toList());
				paths.forEach(System.out::println);
			} else {
				paths = Files.list(Paths.get("C:\\Users\\Admin\\Desktop\\Logs\\All logs")).map(Path::toString)
						.filter(file -> file.contains(serverName) && file.contains(date)).collect(Collectors.toList());
				paths.forEach(System.out::println);
			}
			return paths;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}
	}

	public void writeInFile(File file, BufferedWriter writer) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			writer.write(file.getName() + "\n--------------------------\n\n");
			String line = reader.readLine();
			while (line != null) {

				boolean nextLine = true;
				if ((line.contains("SimpleAsyncTaskExecutor") && line.contains("ERROR"))
						|| (line.contains("scheduling") && line.contains("ERROR"))
						|| (line.contains("main") && line.contains("ERROR"))) {
					this.data = true;
					String[] words = line.split(" ");
					writer.write("Time	: " + words[0] + "\nThread	: " + words[1] + "\nError	: ");
					for (int i = 5; i < words.length; i++) {
						writer.write(words[i] + " ");
					}
					for (int i = 0; i < 4; i++) {
						if ((line = reader.readLine()) != null) {
							if (line.contains("INFO") || line.contains("WARN") || line.contains("ERROR")
									|| line.contains("DEBUG") || line.contains("FATAL") || line.contains("TRACE")) {
								nextLine = false;
								break;
							}
							writer.write("\n	" + line);
						}
					}
					writer.write("\n\n");
				}

				else if (line.contains("setar") && line.contains("ERROR")) {
					this.data = true;
					String[] words = line.split(" ");
					writer.write("Time	: " + words[0] + "\nUserId	: " + words[2] + "\nURL	: " + words[4]
							+ "\nError	: ");
					for (int i = 8; i < words.length; i++) {
						writer.write(words[i] + " ");
					}
					for (int i = 0; i < 18; i++) {
						writer.write("\n" + "	" + reader.readLine());
					}
					writer.write("\n\n");
				}

				else if (line.contains("ERROR")) {
					this.data = true;
					String[] words = line.split(" ");
					writer.write("Time	: " + words[0] + "\nUserId	: " + words[2] + "\nURL	: " + words[4]
							+ "\nError	: ");
					for (int i = 8; i < words.length; i++) {
						writer.write(words[i] + " ");
					}
					for (int i = 0; i < 4; i++) {
						if ((line = reader.readLine()) != null) {
							if (line.contains("INFO") || line.contains("WARN") || line.contains("ERROR")
									|| line.contains("DEBUG") || line.contains("FATAL") || line.contains("TRACE")) {
								nextLine = false;
								break;
							}
							writer.write("\n	" + line);
						}
					}
					writer.write("\n\n");
				}
				if (nextLine)
					line = reader.readLine();

			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}
	}

	public void sendMailWithAttachment(File file, FileDto fileDto) {

		try {
			MimeMessage mimeMessage = javaMailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
			String[] emailTo = fileDto.getEmailTo().toArray(String[]::new);
			mimeMessageHelper.setTo(emailTo);
			String[] emailCc = fileDto.getEmailCc().toArray(String[]::new);
			mimeMessageHelper.setCc(emailCc);
			mimeMessageHelper.setSubject(fileDto.getSubject());
			mimeMessageHelper.setText(fileDto.getBody());
			mimeMessageHelper.addAttachment(file.getName(), file);
			javaMailSender.send(mimeMessage);
			System.out.println("mail sended ----------------");
		} catch (MessagingException e) {
			e.printStackTrace();
			throw new RuntimeException("mail not sended");
		}

	}

}
