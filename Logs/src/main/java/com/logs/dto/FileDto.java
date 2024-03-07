package com.logs.dto;

import java.util.List;

public class FileDto {

	private String serverName;
	private String date;
	private List<String> serverNames;
	private List<String> dates;
	private List<String> emailTo;
	private List<String> emailCc;
	private String subject;
	private String body;
	private String inputLoc;
	private String outputLoc;
	private String errorPattern;
	
	public String getServerName() {
		return serverName;
	}
	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public List<String> getServerNames() {
		return serverNames;
	}
	public void setServerNames(List<String> serverNames) {
		this.serverNames = serverNames;
	}
	public List<String> getDates() {
		return dates;
	}
	public void setDates(List<String> dates) {
		this.dates = dates;
	}
	public List<String> getEmailTo() {
		return emailTo;
	}
	public void setEmailTo(List<String> emailTo) {
		this.emailTo = emailTo;
	}
	public List<String> getEmailCc() {
		return emailCc;
	}
	public void setEmailCc(List<String> emailCc) {
		this.emailCc = emailCc;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public String getInputLoc() {
		return inputLoc;
	}
	public void setInputLoc(String inputLoc) {
		this.inputLoc = inputLoc;
	}
	public String getOutputLoc() {
		return outputLoc;
	}
	public void setOutputLoc(String outputLoc) {
		this.outputLoc = outputLoc;
	}
	public String getErrorPattern() {
		return errorPattern;
	}
	public void setErrorPattern(String errorPattern) {
		this.errorPattern = errorPattern;
	}
	
}
