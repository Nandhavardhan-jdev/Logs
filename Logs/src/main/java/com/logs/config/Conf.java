package com.logs.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {"normal.properties"})
public class Conf {

	@Value("${LogLevel}")
	private String logLevel;
	@Value("${ErrorLog}")
	private String errorLog;
	@Value("${InfoLog}")
	private String infoLog;
	@Value("${WarnLog}")
	private String warnLog;
	@Value("${DebugLog}")
	private String debugLog;
	@Value("${FatalLog}")
	private String fatalLog;
	@Value("${TraceLog}")
	private String traceLog;
	@Value("${SimpleAsyncTaskExecutor}")
	private String simpleAsyncTaskExecutor;
	@Value("${Scheduling}")
	private String scheduling;
	@Value("${Main}")
	private String main;
	@Value("${Setar}")
	private String setar;

	public String getLogLevel() {
		return logLevel;
	}

	public String getErrorLog() {
		return errorLog;
	}

	public String getInfoLog() {
		return infoLog;
	}

	public String getWarnLog() {
		return warnLog;
	}

	public String getDebugLog() {
		return debugLog;
	}

	public String getFatalLog() {
		return fatalLog;
	}

	public String getTraceLog() {
		return traceLog;
	}

	public String getSimpleAsyncTaskExecutor() {
		return simpleAsyncTaskExecutor;
	}

	public String getScheduling() {
		return scheduling;
	}

	public String getMain() {
		return main;
	}

	public String getSetar() {
		return setar;
	}
	

}
