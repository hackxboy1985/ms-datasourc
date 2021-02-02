package org.mints.masterslave.logger;

import org.slf4j.LoggerFactory;

/**
 * 日志
 */
public class MsLogger {

	private static boolean enabled = false;
	private org.slf4j.Logger logger;

	private MsLogger(org.slf4j.Logger logger) {
		this.logger = logger;
	}

	public static void setEnabled(boolean enabled) {MsLogger.enabled = enabled;}

	public static MsLogger getLogger(Class<?> clazz) {
		return new MsLogger(LoggerFactory.getLogger(clazz));
	}

	public void debug(String msg) {
		if(enableLog()) logger.debug(handleString(msg));
	}

	public void debug(String msg, Object... values) {
		if(enableLog()) logger.debug(handleString(msg), values);
	}

	public void debug(String msg, Throwable t)
	{
		if(enableLog()) logger.debug(handleString(msg), t);
	}

	public void info(String msg) {
		if(enableLog()) logger.info(handleString(msg));
	}

	public void info(String msg, Object... values) {
		if(enableLog())  logger.info(handleString(msg), values);
	}

	public void info(String msg, Throwable t)
	{
		if(enableLog()) logger.info(handleString(msg), t);
	}

	public void error(String msg) {
		if(enableLog()) logger.error(handleString(msg));
	}

	public void error(String msg, Object... values) {
		if(enableLog()) logger.error(handleString(msg), values);
	}

	public void error(String msg, Throwable t)
	{
		if(enableLog()) logger.error(handleString(msg), t);
	}

	private String handleString(String msg) {
		return msg;
	}

	//日志开关
	private boolean enableLog(){
		return enabled;
	}

}
