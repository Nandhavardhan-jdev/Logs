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

import com.logs.config.Conf;
import com.logs.dto.FileDto;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	JavaMailSender javaMailSender;

	boolean data;
	
	@Autowired
	Conf conf;

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
		List<String> paths = searchFiles(serverName, date, fileDto.getInputLoc());
		try {
			if (!paths.isEmpty()) {
				File file = new File(
						fileDto.getOutputLoc() + File.separator + "titan-" + serverName + "." + date + ".log");
				BufferedWriter writer;
				writer = new BufferedWriter(new FileWriter(file));
				paths.forEach(path -> writeInFile(new File(path), writer, fileDto));
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
					fileDto.getOutputLoc() + File.separator + "titan-" + serverNames + "." + dates + ".log");
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(file));
			while (sIterator.hasNext()) {
				List<String> paths = searchFiles(sIterator.next(), dIterator.next(), fileDto.getInputLoc());
				if (!paths.isEmpty()) {
					paths.forEach(path -> writeInFile(new File(path), writer, fileDto));
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

	public List<String> searchFiles(String serverName, String date, String inputLoc) {
		LocalDate currentDate = LocalDate.now();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		List<String> paths;
		try {
			if (date.contentEquals(currentDate.format(formatter))) {
				paths = Files.list(Paths.get(inputLoc)).map(Path::toString)
						.filter(file -> file.contains("titan-" + serverName + ".log")).collect(Collectors.toList());
				paths.forEach(System.out::println);
			} else {
				paths = Files.list(Paths.get(inputLoc)).map(Path::toString)
						.filter(file -> file.contains(serverName) && file.contains(date)).collect(Collectors.toList());
				paths.forEach(System.out::println);
			}
			return paths;
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("internal error");
		}
	}

	public void writeInFile(File file, BufferedWriter writer, FileDto fileDto) {
		String errorLog = conf.getErrorLog();
		String infoLog = conf.getInfoLog();
		String warnLog = conf.getWarnLog();
		String debugLog = conf.getDebugLog();
		String fatalLog = conf.getFatalLog();
		String traceLog = conf.getTraceLog();
		boolean fileName = true;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				
				boolean nextLine = true;
				if (line.contains(conf.getLogLevel())) {
					this.data = true;
					if (fileName) {
						writer.write(file.getName() + "\n--------------------------\n\n");
					}
					fileName = false;
					String[] words = line.split("\\s+");
					if(words[4].matches("-")) {
//						System.out.println("thread");
						writer.write("Time	: " + words[0] + "\nThread	: " + words[1] + "\nError	: ");
						for (int i = 5; i < words.length; i++) {
							writer.write(words[i] + " ");
						}
						for (int i = 0; i < 4; i++) {
							if ((line = reader.readLine()) != null) {
								if (line.contains(infoLog) || line.contains(warnLog) || line.contains(errorLog)
										|| line.contains(debugLog) || line.contains(fatalLog) || line.contains(traceLog)) {
									nextLine = false;
									break;
								}
								writer.write("\n	" + line);
							}
						}
						writer.write("\n\n");
					}
					else if(words[7].matches("-")) {
//						System.out.println("with userId");
						writer.write("Time	: " + words[0] + "\nUserId	: " + words[2] + "\nURL	: " + words[4]
								+ "\nError	: ");
						for (int i = 8; i < words.length; i++) {
							writer.write(words[i] + " ");
						}
						for (int i = 0; i < 4; i++) {
							if ((line = reader.readLine()) != null) {
								if (line.contains(infoLog) || line.contains(warnLog) || line.contains(errorLog)
										|| line.contains(debugLog) || line.contains(fatalLog) || line.contains(traceLog)) {
									nextLine = false;
									break;
								}
								writer.write("\n	" + line);
							}
						}
						writer.write("\n\n");
					}
					else if(words[6].matches("-")) {
//						System.out.println("with out userId");
						writer.write("Time	: " + words[0] + "\nUserId	:	\nURL	: " + words[3]
								+ "\nError	: ");
						for (int i = 7; i < words.length; i++) {
							writer.write(words[i] + " ");
						}
						for (int i = 0; i < 4; i++) {
							if ((line = reader.readLine()) != null) {
								if (line.contains(infoLog) || line.contains(warnLog) || line.contains(errorLog)
										|| line.contains(debugLog) || line.contains(fatalLog) || line.contains(traceLog)) {
									nextLine = false;
									break;
								}
								writer.write("\n	" + line);
							}
						}
						writer.write("\n\n");
					}
					
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
	

	public void downloadFiles(FileDto fileDto) {
		String outputLoc = fileDto.getOutputLoc();
		String inputLoc = "C:\\Users\\Admin\\Desktop\\Logs\\23 Feb logs 21 server";
		Iterator<String> sIterator = fileDto.getServerNames().iterator();
		Iterator<String> dIterator = fileDto.getDates().iterator();
		
		while (sIterator.hasNext()) {
			List<String> paths = searchFiles(sIterator.next(), dIterator.next(), inputLoc);
			paths.forEach(paths2 -> {
				try {
					String file = outputLoc + File.separator + new File(paths2).getName();
					Files.copy(Paths.get(paths2), Paths.get(file));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		
	}

}
