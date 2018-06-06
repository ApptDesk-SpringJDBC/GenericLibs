package com.nsrtech.apps.common.logger;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
/**
 * 
 * @author Balaji N
 *
 */
public class LoggerUtil {
	public static Logger getLogger(String loggerFileName, String logFileLocation, String logLevel) throws Exception {
		Logger asynchLogger = null;
		Level level = getLogLevel(logLevel);
		try {
			if (LoggerPool.getHandleToLogger(loggerFileName) == null) {
				asynchLogger = (new AsynchLogger(logFileLocation + "/" + loggerFileName, level)).getLogger(loggerFileName);
				LoggerPool.addLogger(loggerFileName, asynchLogger);
			}
			return LoggerPool.getHandleToLogger(loggerFileName);
		} catch(Exception e) {
			throw e;
		}
	}

	public static Logger changeLogLevel(String loggerFileName, String logFileLocation, String logLevel) throws Exception {
		Logger asynchLogger = null;
		Level level = getLogLevel(logLevel);
		try {
			if (LoggerPool.getHandleToLogger(loggerFileName) != null) {
				asynchLogger = (new AsynchLogger(logFileLocation + "/" + loggerFileName, level)).setLoggerProperties(LoggerPool.getHandleToLogger(loggerFileName), level);
				LoggerPool.addLogger(loggerFileName, asynchLogger);
			}
			return LoggerPool.getHandleToLogger(loggerFileName);
		} catch (Exception e) {
			throw e;
		}
	}
	
	private static Level getLogLevel(String logLevel) {
		Level level = null;
		if (logLevel == null) {
			level = Level.INFO;
		} else {
			if (logLevel.equalsIgnoreCase("TRACE")) {
				level = Level.TRACE;
			} else if (logLevel.equalsIgnoreCase("DEBUG")) {
				level = Level.DEBUG;
			} else {
				level = Level.INFO;
			}
		}
		return level;
	}
}
