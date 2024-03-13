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

}
