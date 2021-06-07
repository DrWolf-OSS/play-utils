package it.drwolf.base.interfaces;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface Loggable {

	static void setLogLevel(String logLevel, String packageName) {
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		loggerContext.getLogger(packageName).setLevel(Level.toLevel(logLevel));
	}

	default Logger logger() {
		return LoggerFactory.getLogger(this.getClass());
	}

}
